package com.graphinout.reader.mermaid.parsers;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Shared parser state for all Mermaid sub-parsers.
 * <p>
 * Tracks created nodes (so the same id is only emitted once), buffers edges (CJ streams require all nodes
 * before any edge), and centralizes error reporting.
 */
public class MermaidParseCtx {

    public final ICjStream writer;
    private final @Nullable Consumer<ContentError> errorHandler;
    private final Map<String, ICjNodeChunkMutable> nodes = new LinkedHashMap<>();
    private final Map<String, String> nodeLabels = new HashMap<>();
    private final List<ICjEdgeChunk> edgeBuffer = new ArrayList<>();
    private int lineNumber = 0;

    public MermaidParseCtx(ICjStream writer, @Nullable Consumer<ContentError> errorHandler) {
        this.writer = writer;
        this.errorHandler = errorHandler;
    }

    public void setLineNumber(int n) {this.lineNumber = n;}

    public int lineNumber() {return lineNumber;}

    /** Ensure a node with this id has been created. Returns true if it was newly created. */
    public boolean ensureNode(String id) {
        return ensureNode(id, null);
    }

    /** Ensure a node with this id has been created, optionally setting a label if not already set. */
    public boolean ensureNode(String id, @Nullable String label) {
        ICjNodeChunkMutable n = nodes.get(id);
        boolean isNew = false;
        if (n == null) {
            n = writer.createNodeChunk();
            n.id(id);
            nodes.put(id, n);
            isNew = true;
        }
        if (label != null && !label.isEmpty() && !nodeLabels.containsKey(id)) {
            n.addLabelWithoutLanguage(label);
            nodeLabels.put(id, label);
        }
        return isNew;
    }

    /** Buffer an edge from source -> target with optional label. */
    public void addEdge(String source, String target, @Nullable String label) {
        ensureNode(source);
        ensureNode(target);
        ICjEdgeChunkMutable edge = writer.createEdgeChunk();
        edge.addEndpoint(ep -> ep.node(source).direction(CjDirection.IN));
        edge.addEndpoint(ep -> ep.node(target).direction(CjDirection.OUT));
        if (label != null && !label.isEmpty()) {
            edge.addLabelWithoutLanguage(label);
        }
        edgeBuffer.add(edge);
    }

    /** Emit all buffered nodes then all buffered edges to the writer. */
    public void flush() {
        for (ICjNodeChunkMutable n : nodes.values()) writer.node(n);
        for (ICjEdgeChunk e : edgeBuffer) writer.edge(e);
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
        return !nodes.isEmpty() || !edgeBuffer.isEmpty();
    }
}
