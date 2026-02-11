package com.graphinout.base.cj.stream;

import com.google.common.collect.Sets;
import com.graphinout.base.cj.factory.CjFactory;
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
import com.graphinout.foundation.pure.json.writer.impl.ValidatingJsonWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;

import org.jspecify.annotations.Nullable;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValidatingCjStream extends ValidatingJsonWriter implements ICjStream {

    private final Set<String> nodeIds = new HashSet<>();
    private final Set<String> edgeIds = new HashSet<>();
    private final Set<String> portIds = new HashSet<>();
    private final Set<String> endpointNodeIds = new HashSet<>();
    private final Set<String> endpointPortIds = new HashSet<>();

    private final CjFactory factory = new CjFactory();

    private void resetGraphScope() {
        nodeIds.clear();
        edgeIds.clear();
        portIds.clear();
        endpointNodeIds.clear();
        endpointPortIds.clear();
    }

    // ========== ICjStream events ==========

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
    public void documentEnd() {
        // no-op
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // Each graph has its own id space for nodes/edges/ports
        resetGraphScope();
    }

    @Override
    public void graphEnd() {
        Set<String> missingNodeIds = Sets.difference(endpointNodeIds, nodeIds);
        if (!missingNodeIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent node ID(s): " + missingNodeIds);
        }

        Set<String> missingPortIds = Sets.difference(endpointPortIds, portIds);
        if (!missingPortIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent port ID(s): " + missingPortIds);
        }
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        // Track node id duplicates
        @Nullable String id = node.id();
        if (id != null) {
            if (!nodeIds.add(id)) {
                throw new IllegalStateException("Duplicate node ID: " + id);
            }
        }
        // Collect port IDs recursively
        node.ports().forEach(this::collectPortIdsRecursively);
    }

    @Override
    public void nodeEnd() {
        // no-op
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        @Nullable String id = edge.id();
        if (id != null) {
            if (!edgeIds.add(id)) {
                throw new IllegalStateException("Duplicate edge ID: " + id);
            }
        }
        // Remember referenced node/port ids from endpoints for validation at graphEnd
        edge.endpoints().forEach(this::collectEndpointRefs);
    }

    @Override
    public void edgeEnd() {
        // no-op
    }

    // ========== ICjFactory delegation ==========

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return factory.createDocumentChunk();
    }

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
    public IJsonFactory jsonFactory() {
        return factory.jsonFactory();
    }

    // ========== helpers ==========

    private void collectPortIdsRecursively(ICjPort port) {
        String id = port.id();
        if (id != null) {
            portIds.add(id);
        }
        port.ports().forEach(this::collectPortIdsRecursively);
    }

    private void collectEndpointRefs(ICjEndpoint ep) {
        String node = ep.node();
        if (node != null) endpointNodeIds.add(node);
        String port = ep.port();
        if (port != null) endpointPortIds.add(port);
    }
}
