package com.graphinout.base.cj.writer;

import com.google.common.collect.Sets;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.foundation.json.writer.impl.ValidatingJsonWriter;

import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ValidatingCjWriter extends ValidatingJsonWriter implements ICjWriter {

    private enum Context {
        GRAPH, NODE, EDGE, PORT, ENDPOINT, LABEL_ENTRY, CONNECTED_JSON
    }

    private final Deque<Context> contextStack = new ArrayDeque<>();

    private final Set<String> nodeIds = new HashSet<>();
    private final Set<String> edgeIds = new HashSet<>();
    private final Set<String> portIds = new HashSet<>();
    private final Set<String> endpointNodeIds = new HashSet<>();
    private final Set<String> endpointPortIds = new HashSet<>();

    @Override
    public void baseUri(String baseUri) {
        if (baseUri != null && !baseUri.isEmpty()) {
            try {
                new java.net.URI(baseUri);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid baseUri: " + baseUri, e);
            }
        }
    }

    @Override
    public void id(String id) {
        if (contextStack.isEmpty()) {
            throw new IllegalStateException("id() called outside of a known context");
        }
        Context currentContext = contextStack.peek();
        switch (currentContext) {
            case NODE:
                if (!nodeIds.add(id)) {
                    throw new IllegalStateException("Duplicate node ID: " + id);
                }
                break;
            case EDGE:
                if (!edgeIds.add(id)) {
                    throw new IllegalStateException("Duplicate edge ID: " + id);
                }
                break;
            case PORT:
                portIds.add(id);
                break;
            default:
                // Not an ID we track for validation
                break;
        }
    }

    @Override
    public void nodeId(String nodeId) {
        if (!contextStack.isEmpty() && contextStack.peek() == Context.ENDPOINT) {
            endpointNodeIds.add(nodeId);
        }
    }

    @Override
    public void portId(String portId) {
        if (!contextStack.isEmpty() && contextStack.peek() == Context.ENDPOINT) {
            endpointPortIds.add(portId);
        }
    }

    @Override
    public void graphEnd() throws CjException {
        Set<String> missingNodeIds = Sets.difference(endpointNodeIds, nodeIds);
        if (!missingNodeIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent node ID(s): " + missingNodeIds);
        }

        Set<String> missingPortIds = Sets.difference(endpointPortIds, portIds);
        if (!missingPortIds.isEmpty()) {
            throw new IllegalStateException("Edge endpoint references non-existent port ID(s): " + missingPortIds);
        }
        contextStack.pop();
    }

    @Override
    public void connectedJsonStart() {
        contextStack.push(Context.CONNECTED_JSON);
    }

    @Override
    public void connectedJsonEnd() {
        contextStack.pop();
    }

    @Override
    public void graphStart() throws CjException {
        contextStack.push(Context.GRAPH);
    }

    @Override
    public void nodeStart() {
        contextStack.push(Context.NODE);
    }

    @Override
    public void nodeEnd() {
        contextStack.pop();
    }

    @Override
    public void edgeStart() {
        contextStack.push(Context.EDGE);
    }

    @Override
    public void edgeEnd() {
        contextStack.pop();
    }

    @Override
    public void portStart() {
        contextStack.push(Context.PORT);
    }

    @Override
    public void portEnd() {
        contextStack.pop();
    }

    @Override
    public void endpointStart() {
        contextStack.push(Context.ENDPOINT);
    }

    @Override
    public void endpointEnd() {
        contextStack.pop();
    }

    @Override
    public void labelEntryStart() {
        contextStack.push(Context.LABEL_ENTRY);
    }

    @Override
    public void labelEntryEnd() {
        contextStack.pop();
    }

    @Override
    public void connectedJson__canonical(boolean b) {
    }

    @Override
    public void connectedJson__versionDate(String s) {
    }

    @Override
    public void connectedJson__versionNumber(String s) {
    }

    @Override
    public void direction(CjDirection direction) {
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
    }

    @Override
    public void jsonDataEnd() {
    }

    @Override
    public void jsonDataStart() {
    }

    @Override
    public void language(String language) {
    }

    @Override
    public void listEnd(CjType cjType) {
    }

    @Override
    public void listStart(CjType cjType) {
    }

    @Override
    public void value(String value) {
    }
}
