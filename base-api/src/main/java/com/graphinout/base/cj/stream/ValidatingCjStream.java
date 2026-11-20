package com.graphinout.base.cj.stream;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.util.Utils;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.writer.impl.ValidatingJsonWriter;
import org.jspecify.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates a CJ stream. Ids live in one document-wide space (an edge may reference a node of any graph, including a
 * nested or an outer one, or — edges on edges — another edge), so endpoint references are checked at
 * {@link #documentEnd()}. Duplicate node/edge ids are checked within each graph.
 */
public class ValidatingCjStream extends ValidatingJsonWriter implements ICjStream {

    /** node/edge ids of each open graph, innermost on top, for the per-graph duplicate check */
    private final java.util.Deque<Set<String>> graphNodeIds = new java.util.ArrayDeque<>();
    private final java.util.Deque<Set<String>> graphEdgeIds = new java.util.ArrayDeque<>();
    private final Set<String> nodeIds = new HashSet<>();
    private final Set<String> edgeIds = new HashSet<>();
    private final Set<String> portIds = new HashSet<>();
    private final Set<String> endpointNodeIds = new HashSet<>();
    private final Set<String> endpointPortIds = new HashSet<>();

    private final CjFactory factory = new CjFactory();

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return factory.createDocumentChunk();
    }

    // ========== ICjStream events ==========

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return factory.createEdgeChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return factory.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return factory.createNodeChunk();
    }

    @Override
    public void documentEnd() {
        // an endpoint may reference a node or, for edges on edges, another edge
        Set<String> missingNodeIds = Utils.setsDifference(Utils.setsDifference(endpointNodeIds, nodeIds), edgeIds);
        if (!missingNodeIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent node or edge ID(s): " + missingNodeIds);
        }
        Set<String> missingPortIds = Utils.setsDifference(endpointPortIds, portIds);
        if (!missingPortIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent port ID(s): " + missingPortIds);
        }
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // validate @context namespace URIs if present
        Map<String, String> context = document.context();
        if (context != null) {
            for (Map.Entry<String, String> entry : context.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    try {
                        new java.net.URI(value);
                    } catch (URISyntaxException e) {
                        throw new IllegalStateException("Invalid namespace URI in @context for prefix '" + entry.getKey() + "': " + value, e);
                    }
                }
            }
        }
    }

    @Override
    public void edgeEnd() {
        // no-op
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        @Nullable String id = edge.id();
        if (id != null) {
            if (!currentGraph(graphEdgeIds).add(id)) {
                throw new IllegalStateException("Duplicate edge ID: " + id);
            }
            edgeIds.add(id);
        }
        // Remember referenced node/port ids from endpoints for validation at graphEnd
        edge.endpoints().forEach(this::collectEndpointRefs);
    }

    @Override
    public void graphEnd() {
        graphNodeIds.pop();
        graphEdgeIds.pop();
    }

    // ========== ICjFactory delegation ==========

    @Override
    public void graphStart(ICjGraphChunk graph) {
        graphNodeIds.push(new HashSet<>());
        graphEdgeIds.push(new HashSet<>());
    }

    @Override
    public IJsonFactory jsonFactory() {
        return factory.jsonFactory();
    }

    @Override
    public void nodeEnd() {
        // no-op
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        // Track node id duplicates
        @Nullable String id = node.id();
        if (id != null) {
            if (!currentGraph(graphNodeIds).add(id)) {
                throw new IllegalStateException("Duplicate node ID: " + id);
            }
            nodeIds.add(id);
        }
        // Collect port IDs recursively
        node.ports().forEach(this::collectPortIdsRecursively);
    }

    private void collectEndpointRefs(ICjEndpoint ep) {
        String node = ep.node();
        if (node != null) endpointNodeIds.add(node);
        String port = ep.port();
        if (port != null) endpointPortIds.add(port);
    }

    // ========== helpers ==========

    private void collectPortIdsRecursively(ICjPort port) {
        String id = port.id();
        if (id != null) {
            portIds.add(id);
        }
        port.ports().forEach(this::collectPortIdsRecursively);
    }

    private static Set<String> currentGraph(java.util.Deque<Set<String>> perGraph) {
        if (perGraph.isEmpty()) throw new IllegalStateException("node or edge outside of a graph");
        return perGraph.peek();
    }

}
