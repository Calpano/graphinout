package com.graphinout.base.validation;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioEndpoint;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.json.impl.ValidatingJsonWriter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/** QUALITY: Unfinished */
public class ValidatingGioWriter extends ValidatingJsonWriter implements GioWriter {

    public enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, HYPEREDGE, DESC, DATA, EDGE, PORT, LOCATOR, DEFAULT, ENDPOINT;

        static {
            EMPTY.allowedChildren.add(GRAPHML);
            GRAPHML.allowedChildren.addAll(List.of(DATA, DESC, KEY, GRAPH));
            KEY.allowedChildren.addAll(List.of(DESC, DEFAULT));
            NODE.allowedChildren.addAll(List.of(DATA, DESC, GRAPH, LOCATOR, PORT));
            GRAPH.allowedChildren.addAll(List.of(DATA, DESC, NODE, EDGE, HYPEREDGE, LOCATOR));
            EDGE.allowedChildren.addAll(List.of(DATA, DESC, GRAPH));
            HYPEREDGE.allowedChildren.addAll(List.of(DATA, DESC, ENDPOINT));
            PORT.allowedChildren.addAll(List.of(DATA, PORT));
        }

        // IMPROVE remove LinkedHashSet and use HashSet instead when no tests rely on this order
        private final Set<CurrentElement> allowedChildren = new LinkedHashSet<>();

        public boolean isValidChild(CurrentElement childElement) {
            return allowedChildren.contains(childElement);
        }
    }

    private static final Logger log = getLogger(ValidatingGioWriter.class);
    /**
     * remember elements we saw already; nesting order in stack
     */
    private final Deque<CurrentElement> currentElements;
    private final Set<String> existingNodeIds = new HashSet<>();
    private final Set<String> existingEdgeIds = new HashSet<>();
    private final Map<String, List<String>> nonExistingNode = new HashMap<>();
    private final Set<String> existingPortNames = new HashSet<>();
    private final Map<String, List<String>> nonExistingPortNames = new HashMap<>();
    private final Set<String> existingKeyIds = new HashSet<>();

    public ValidatingGioWriter() {
        this.currentElements = new LinkedList<>();
        // never deal with an empty stack
        currentElements.push(CurrentElement.EMPTY);
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        if (baseUri != null && !baseUri.isEmpty()) {
            try {
                new java.net.URI(baseUri);
            } catch (java.net.URISyntaxException e) {
                throw new IllegalStateException("Invalid baseuri: " + baseUri, e);
            }
        }
    }

    @Override
    public void data(GioData data) throws IOException {
        ensureAllowedStart(CurrentElement.DATA);
        ensureAllowedEnd(CurrentElement.DATA);
        if (data.getKey() == null) {
            throw new IllegalArgumentException("Gio data must have a key attribute.");
        }
    }

    @Override
    public void endDocument() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPHML);
        if (!nonExistingNode.isEmpty()) {
            nonExistingNode.forEach((s, messages) -> messages.forEach(log::warn));
            throw new IllegalStateException(nonExistingNode.size() + " nodes used in the graph without reference.");
        }
        if (!nonExistingPortNames.isEmpty()) {
            nonExistingPortNames.forEach((s, messages) -> messages.forEach(log::warn));
            throw new IllegalStateException(nonExistingPortNames.size() + " ports used in the graph without reference.");
        }
    }

    @Override
    public void endEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.GRAPH);
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.NODE);
    }

    @Override
    public void endPort() throws IOException {
        ensureAllowedEnd(CurrentElement.PORT);
    }

    public void key(GioKey key) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(key);
        ensureAllowedEnd(CurrentElement.KEY);
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
    }

    @Override
    public void startEdge(GioEdge hyperEdge) throws IOException {
        ensureAllowedStart(CurrentElement.HYPEREDGE);
        validateEdge(hyperEdge);
        investigatingTheExistenceOfTheNode(hyperEdge);
        investigatingTheExistenceOfThePort(hyperEdge);
        existingEdgeIds.add(hyperEdge.getId());
    }

    @Override
    public void startGraph(GioGraph graph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
        existingNodeIds.add(node.getId());
        nonExistingNode.remove(node.getId());
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        // FIXME port names are not unique per graph, but only unique within a node
        ensureAllowedStart(CurrentElement.PORT);
        if (port.getName() == null || port.getName().trim().isEmpty())
            throw new IllegalStateException("Name of Port cannot be null or empty.");

        if (existingPortNames.contains(port.getName()))
            throw new IllegalStateException("Port must have a unique Name, but name is used several times: '" + port.getName() + "'.");
        existingPortNames.add(port.getName());
        nonExistingPortNames.remove(port.getName());
    }

    private void ensureAllowedEnd(CurrentElement element) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        if (currentElement != element) {
            throw new GioWriterEndException(element, currentElement, "Wrong order of calls. Cannot END '" + element + "', last started element was " + currentElement);
        }
        currentElements.pop();
        if (log.isDebugEnabled()) log.debug("ENDED '{} '. Stack: {}", element.name(), stackToString());
    }

    private void ensureAllowedStart(CurrentElement childElement) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        assert currentElement != null;
        if (!currentElement.isValidChild(childElement)) {
            throw new GioWriterStartException(currentElement, childElement, "Wrong order of elements. In element '" + currentElement + "' expected one of " + currentElement.allowedChildren + " but found " + childElement + ". Stack (leaf-to-root): " + this.currentElements);
        }
        currentElements.push(childElement);
        if (log.isDebugEnabled()) log.debug("STARTED '{}' => Stack: {}", childElement.name(), stackToString());
    }

    private void investigatingTheExistenceOfTheNode(GioEdge hyperEdge) throws IllegalStateException {
        List<GioEndpoint> endpoints = hyperEdge.getEndpoints();
        for (GioEndpoint endpoint : endpoints) {

            if (!existingNodeIds.contains(endpoint.getNode())) {
                String nodeId = endpoint.getNode();
                nonExistingNode.computeIfAbsent(nodeId, key -> new ArrayList<>()) //
                        .add(" Edge [" + hyperEdge + "] references to a non-existent node ID: '" + nodeId + "'");
            } else nonExistingNode.remove(endpoint.getNode());
        }
    }

    private void investigatingTheExistenceOfThePort(@Nonnull GioEdge hyperEdge) throws IllegalStateException {
        for (GioEndpoint endpoint : hyperEdge.getEndpoints()) {
            if (endpoint.getPort() != null) if (!existingPortNames.contains(endpoint.getPort())) {
                nonExistingPortNames.computeIfAbsent(endpoint.getPort(), key -> new ArrayList<>()) //
                        .add(" Edge [" + hyperEdge + "] references to a non-existent port Name: '" + endpoint.getPort() + "'");
            } else nonExistingNode.remove(endpoint.getPort());
        }
    }

    private String stackToString() {
        List<String> list = currentElements.stream().map(Enum::name).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(list);
        return String.join("|", list);
    }

    private void validateData(GioData data) throws IllegalStateException {
        String key = data.getKey();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Data key cannot be null or empty.");
        }
    }

    private void validateEdge(GioEdge hyperEdge) throws IllegalStateException {
        List<GioEndpoint> hyperEdgeEndpoints = hyperEdge.getEndpoints();
        if (hyperEdgeEndpoints == null || hyperEdgeEndpoints.size() < 2)
            throw new IllegalStateException(" edge must have at least 2 endpoints: " + hyperEdge);
    }

    private void validateKey(GioKey key) throws IllegalStateException {
        String keyId = key.getId();
        if (existingKeyIds.contains(keyId)) {
            throw new IllegalStateException("Key ID already exists: " + keyId + ". ID must be unique.");
        } else {
            existingKeyIds.add(keyId);
        }
    }

    private void validateLocator(URL locator) throws IllegalStateException {
        if (locator == null) return;
        ensureAllowedStart(CurrentElement.LOCATOR);
        ensureAllowedEnd(CurrentElement.LOCATOR);
    }

    private void validateNode(GioNode node) throws IllegalStateException {
        String nodeId = node.getId();
        if (nodeId == null || nodeId.isEmpty()) throw new IllegalStateException("Node must have an ID.");
        if (existingNodeIds.contains(nodeId)) throw new IllegalStateException("Node ID must be unique.");
    }

}
