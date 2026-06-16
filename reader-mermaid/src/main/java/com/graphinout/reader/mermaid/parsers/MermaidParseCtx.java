package com.graphinout.reader.mermaid.parsers;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shared parser state for all Mermaid sub-parsers.
 * <p>
 * Builds an in-memory tree of nodes, edges and {@code subgraph} blocks while parsing, then streams it to the
 * {@link ICjStream} on {@link #flush()}. Subgraph blocks become CJ graphs nested inside a node (so they round-trip
 * as the {@code nested-graphs-in-nodes} feature). Top-level (non-nested) nodes and edges are emitted via the
 * convenience {@code node()/edge()} stream calls, preserving existing reader-test expectations.
 */
public class MermaidParseCtx {

    /** A node, edge or subgraph specification gathered during parsing. */
    private static final class NodeSpec {
        final String id;
        @Nullable String label;
        /** Non-null when this node is actually a {@code subgraph} container. */
        @Nullable Scope subgraph;

        NodeSpec(String id) {this.id = id;}
    }

    private static final class EdgeSpec {
        final String source;
        final String target;
        @Nullable final String label;
        final boolean directed;

        EdgeSpec(String source, String target, @Nullable String label, boolean directed) {
            this.source = source;
            this.target = target;
            this.label = label;
            this.directed = directed;
        }
    }

    /** One graph scope: ordered nodes (by id) plus its edges, and an optional title (for subgraph blocks). */
    private static final class Scope {
        @Nullable String label;
        final Map<String, NodeSpec> nodes = new LinkedHashMap<>();
        final Set<String> labelled = new HashSet<>();
        final List<EdgeSpec> edges = new ArrayList<>();
    }

    public final ICjStream writer;
    private final @Nullable Consumer<ContentError> errorHandler;
    private final Scope root = new Scope();
    private final Deque<Scope> scopeStack = new ArrayDeque<>();
    /** The subgraph container nodes, by depth, so addEdge can attach to the open subgraph's parent node. */
    private final Deque<NodeSpec> subgraphStack = new ArrayDeque<>();
    /** Mermaid node ids are global: this maps each known id to the scope that owns its declaration. */
    private final Map<String, Scope> nodeOwner = new LinkedHashMap<>();
    private int subgraphCounter = 0;
    private int lineNumber = 0;

    public MermaidParseCtx(ICjStream writer, @Nullable Consumer<ContentError> errorHandler) {
        this.writer = writer;
        this.errorHandler = errorHandler;
        scopeStack.push(root);
    }

    public void setLineNumber(int n) {this.lineNumber = n;}

    public int lineNumber() {return lineNumber;}

    private Scope current() {return scopeStack.peek();}

    /** Begin a {@code subgraph} block. The block becomes a CJ graph nested in a node carrying {@code id}. */
    public void beginSubgraph(@Nullable String id, @Nullable String label) {
        String nodeId = (id != null && !id.isBlank()) ? id : ("subgraph_" + (++subgraphCounter));
        // The subgraph container is a node in the current scope; its nested graph is a fresh child scope.
        Scope owner = nodeOwner.get(nodeId);
        if (owner == null) {
            owner = current();
            owner.nodes.put(nodeId, new NodeSpec(nodeId));
            nodeOwner.put(nodeId, owner);
        }
        NodeSpec container = owner.nodes.get(nodeId);
        Scope child = new Scope();
        container.subgraph = child;
        // The subgraph title is the nested graph's label (the writer emits the graph label as the block title).
        child.label = (label != null && !label.isEmpty()) ? label : null;
        subgraphStack.push(container);
        scopeStack.push(child);
    }

    /** End the innermost {@code subgraph} block. */
    public void endSubgraph() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
            subgraphStack.pop();
        }
    }

    /** Ensure a node with this id has been created. Returns true if it was newly created. */
    public boolean ensureNode(String id) {
        return ensureNode(id, null);
    }

    /**
     * Ensure a node with this id exists, optionally setting a label. Mermaid node ids are global: a node is owned by
     * the scope where it is first mentioned, and later mentions (e.g. an edge endpoint inside another scope) reference
     * that same node instead of creating a duplicate.
     */
    public boolean ensureNode(String id, @Nullable String label) {
        Scope owner = nodeOwner.get(id);
        boolean isNew = owner == null;
        if (owner == null) {
            owner = current();
            owner.nodes.put(id, new NodeSpec(id));
            nodeOwner.put(id, owner);
        }
        if (label != null && !label.isEmpty() && !owner.labelled.contains(id)) {
            owner.nodes.get(id).label = label;
            owner.labelled.add(id);
        }
        return isNew;
    }

    /** Buffer a directed edge from source -> target with optional label. */
    public void addEdge(String source, String target, @Nullable String label) {
        addEdge(source, target, label, true);
    }

    /** Buffer an edge from source -> target with optional label; {@code directed} selects in/out vs undir endpoints. */
    public void addEdge(String source, String target, @Nullable String label, boolean directed) {
        ensureNode(source);
        ensureNode(target);
        current().edges.add(new EdgeSpec(source, target, label, directed));
    }

    /** Emit the gathered tree: top-level nodes then edges; subgraph containers stream nested graphs. */
    public void flush() {
        emitScope(root, true);
    }

    private void emitScope(Scope scope, boolean topLevel) {
        for (NodeSpec n : scope.nodes.values()) {
            if (n.subgraph == null) {
                if (topLevel) {
                    writer.node(nodeChunk(n));
                } else {
                    writer.nodeStart(nodeChunk(n));
                    writer.nodeEnd();
                }
            } else {
                writer.nodeStart(nodeChunk(n));
                ICjGraphChunkMutable g = writer.createGraphChunk();
                g.id(n.id + "__graph");
                if (n.subgraph.label != null && !n.subgraph.label.isEmpty()) {
                    g.addLabelWithoutLanguage(n.subgraph.label);
                }
                writer.graphStart(g);
                emitScope(n.subgraph, false);
                writer.graphEnd();
                writer.nodeEnd();
            }
        }
        for (EdgeSpec e : scope.edges) {
            emitEdge(e, topLevel);
        }
    }

    private ICjNodeChunkMutable nodeChunk(NodeSpec n) {
        ICjNodeChunkMutable chunk = writer.createNodeChunk();
        chunk.id(n.id);
        if (n.label != null && !n.label.isEmpty()) chunk.addLabelWithoutLanguage(n.label);
        return chunk;
    }

    private void emitEdge(EdgeSpec e, boolean topLevel) {
        var edge = writer.createEdgeChunk();
        if (e.directed) {
            edge.addEndpoint(ep -> ep.node(e.source).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(e.target).direction(CjDirection.OUT));
        } else {
            edge.addEndpoint(ep -> ep.node(e.source).direction(CjDirection.UNDIR));
            edge.addEndpoint(ep -> ep.node(e.target).direction(CjDirection.UNDIR));
        }
        if (e.label != null && !e.label.isEmpty()) {
            edge.addLabelWithoutLanguage(e.label);
        }
        if (topLevel) {
            writer.edge(edge);
        } else {
            writer.edgeStart(edge);
            writer.edgeEnd();
        }
    }

    public void warn(String msg) {
        send(ContentError.ErrorLevel.Warn, msg);
    }

    public void info(String msg) {
        send(ContentError.ErrorLevel.Info, msg);
    }

    private void send(ContentError.ErrorLevel level, String msg) {
        ContentError err = ContentError.of(level, msg, Location.of(lineNumber, 1));
        Nullables.ifConsumerPresentAccept(errorHandler, err);
    }

    public boolean hasContent() {
        return !root.nodes.isEmpty() || !root.edges.isEmpty();
    }
}
