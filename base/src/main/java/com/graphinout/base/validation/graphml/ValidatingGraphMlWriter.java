package com.graphinout.base.validation.graphml;

import com.graphinout.base.BaseOutput;
import com.graphinout.base.graphml.GraphmlWriter;
import com.graphinout.base.graphml.IGraphmlData;
import com.graphinout.base.graphml.IGraphmlDocument;
import com.graphinout.base.graphml.IGraphmlEdge;
import com.graphinout.base.graphml.IGraphmlEndpoint;
import com.graphinout.base.graphml.IGraphmlGraph;
import com.graphinout.base.graphml.IGraphmlHyperEdge;
import com.graphinout.base.graphml.IGraphmlKey;
import com.graphinout.base.graphml.IGraphmlLocator;
import com.graphinout.base.graphml.IGraphmlNode;
import com.graphinout.base.graphml.IGraphmlPort;
import com.graphinout.base.graphml.impl.GraphmlData;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class ValidatingGraphMlWriter extends BaseOutput implements GraphmlWriter {

    public enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, HYPEREDGE, DESC, DATA, EDGE, PORT, LOCATOR, DEFAULT, ENDPOINT;

        static {
            EMPTY.allowedChildren.addAll(Arrays.asList(GRAPHML));
            GRAPHML.allowedChildren.addAll(Arrays.asList(DATA, DESC, KEY, GRAPH));
            KEY.allowedChildren.addAll(Arrays.asList(DESC, DEFAULT));
            NODE.allowedChildren.addAll(Arrays.asList(DATA, DESC, GRAPH, LOCATOR, PORT));
            GRAPH.allowedChildren.addAll(Arrays.asList(DATA, DESC, NODE, EDGE, HYPEREDGE, LOCATOR));
            EDGE.allowedChildren.addAll(Arrays.asList(DATA, DESC, GRAPH));
            HYPEREDGE.allowedChildren.addAll(Arrays.asList(DATA, DESC, ENDPOINT));
            PORT.allowedChildren.addAll(Arrays.asList(DATA, PORT));
        }

        // IMPROVE remove LinkedHashSet and use HashSet instead when no tests rely on this order
        private Set<CurrentElement> allowedChildren = new LinkedHashSet<>();

        public boolean isValidChild(CurrentElement childElement) {
            return allowedChildren.contains(childElement);
        }
    }

    private static final Logger log = getLogger(ValidatingGraphMlWriter.class);
    /**
     * remember elements we saw already; nesting order in stack
     */
    private final Deque<CurrentElement> currentElements;
    private final Set<String> existingNodeIds = new HashSet<>();
    private final Set<String> existingEdgeIds = new HashSet<>();
    private final Map<String, List<String>> nonExistingNode = new HashMap<>();
    private final Set<String> existingPortNames = new HashSet<>();
    private final Map<String, List<String>> nonExistingPortNames = new HashMap<>();
    private Set<String> existingKeyIds = new HashSet<>();

    public ValidatingGraphMlWriter() {
        this.currentElements = new LinkedList<>();
        // never deal with an empty stack
        currentElements.push(CurrentElement.EMPTY);
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        ensureAllowedStart(CurrentElement.DATA);
        ensureAllowedEnd(CurrentElement.DATA);
        if (data.key() == null) {
            throw new IllegalArgumentException("Graphml data must have a key attribute.");
        }
    }

    @Override
    public void documentEnd() throws IOException {
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
    public void documentStart(IGraphmlDocument document) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
        validateGraphMl(document);
    }

    @Override
    public void edgeEnd() throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
    }

    @Override
    public void edgeStart(IGraphmlEdge edge) throws IOException {
        ensureAllowedStart(CurrentElement.EDGE);
        validateEdge(edge);
        investigatingTheExistenceOfTheNode(edge);
        if (edge.id() != null) existingEdgeIds.add(edge.id());

        investigatingTheExistenceOfThePort(edge);
    }

    @Override
    public void graphEnd() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPH);
    }

    @Override
    public void graphStart(IGraphmlGraph graph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
        validateLocator(graph.locator());
    }

    @Override
    public void hyperEdgeEnd() throws IOException {
        ensureAllowedEnd(CurrentElement.HYPEREDGE);
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge hyperEdge) throws IOException {
        ensureAllowedStart(CurrentElement.HYPEREDGE);
        validateHyperEdge(hyperEdge);
        investigatingTheExistenceOfTheNode(hyperEdge);
        investigatingTheExistenceOfThePort(hyperEdge);
        existingEdgeIds.add(hyperEdge.id());
    }

    public void key(IGraphmlKey key) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(key);
        ensureAllowedEnd(CurrentElement.KEY);
    }

    @Override
    public void nodeEnd() throws IOException {
        ensureAllowedEnd(CurrentElement.NODE);
    }

    @Override
    public void nodeStart(IGraphmlNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
        existingNodeIds.add(node.id());
        nonExistingNode.remove(node.id());
    }

    @Override
    public void portEnd() throws IOException {
        ensureAllowedEnd(CurrentElement.PORT);
    }

    @Override
    public void portStart(IGraphmlPort port) throws IOException {
        // FIXME port names are not unique per graph, but only unique within a node
        ensureAllowedStart(CurrentElement.PORT);
        if (port.name() == null || port.name().trim().isEmpty())
            throw new IllegalStateException("Name of Port cannot be null or empty.");

        if (existingPortNames.contains(port.name()))
            throw new IllegalStateException("Port must have a unique Name, but name is used several times: '" + port.name() + "'.");
        existingPortNames.add(port.name());
        nonExistingPortNames.remove(port.name());
    }

    private void ensureAllowedEnd(CurrentElement element) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        if (currentElement != element) {
            throw new GraphmlWriterEndException(element, currentElement, "Wrong order of calls. Cannot END '" + element + "', last started element was " + currentElement);
        }
        currentElements.pop();
        if (log.isDebugEnabled()) log.debug("ENDED '{} '. Stack: {}", element.name(), stackToString());
    }

    private void ensureAllowedStart(CurrentElement childElement) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        assert currentElement != null;
        if (!currentElement.isValidChild(childElement)) {
            throw new GraphmlWriterStartException(currentElement, childElement, "Wrong order of elements. In element '" + currentElement + "' expected one of " + currentElement.allowedChildren + " but found " + childElement + ". Stack (leaf-to-root): " + this.currentElements);
        }
        currentElements.push(childElement);
        if (log.isDebugEnabled()) log.debug("STARTED '{}' => Stack: {}", childElement.name(), stackToString());
    }

    /**
     * @param edge
     **/
    private void investigatingTheExistenceOfTheNode(IGraphmlEdge edge) throws IllegalStateException {
        for (String nodeId : Arrays.asList(edge.source(), edge.target())) {
            if (!existingNodeIds.contains(nodeId)) {
                nonExistingNode.computeIfAbsent(nodeId, key -> new ArrayList<>()) //
                        .add("Edge [" + edge + "] references to a non-existent node ID: '" + nodeId + "'");
            } else nonExistingNode.remove(nodeId);
        }
    }

    /**
     * @param hyperEdge
     */
    private void investigatingTheExistenceOfTheNode(IGraphmlHyperEdge hyperEdge) throws IllegalStateException {
        List<IGraphmlEndpoint> endpoints = hyperEdge.endpoints();
        for (IGraphmlEndpoint endpoint : endpoints) {

            if (!existingNodeIds.contains(endpoint.node())) {
                String nodeId = endpoint.node();
                nonExistingNode.computeIfAbsent(nodeId, key -> new ArrayList<>()) //
                        .add("Hyper Edge [" + hyperEdge + "] references to a non-existent node ID: '" + nodeId + "'");
            } else nonExistingNode.remove(endpoint.node());
        }
    }

    private void investigatingTheExistenceOfThePort(@Nonnull IGraphmlEdge edge) throws IllegalStateException {
        for (String portName : Arrays.asList(edge.sourcePort(), edge.targetPort())) {
            if (portName != null) if (!existingPortNames.contains(portName)) {
                nonExistingPortNames.computeIfAbsent(portName, key -> new ArrayList<>()) //
                        .add("Edge [" + edge + "] references to a non-existent port Name: '" + portName + "'");
            } else nonExistingNode.remove(portName);
        }
    }

    private void investigatingTheExistenceOfThePort(@Nonnull IGraphmlHyperEdge hyperEdge) throws IllegalStateException {
        for (IGraphmlEndpoint endpoint : hyperEdge.endpoints()) {
            if (endpoint.port() != null) if (!existingPortNames.contains(endpoint.port())) {
                nonExistingPortNames.computeIfAbsent(endpoint.port(), key -> new ArrayList<>()) //
                        .add("Hyper Edge [" + hyperEdge + "] references to a non-existent port Name: '" + endpoint.port() + "'");
            } else nonExistingNode.remove(endpoint.port());
        }
    }

    private String stackToString() {
        List<String> list = currentElements.stream().map(Enum::name).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(list);
        return String.join("|", list);
    }

    private void validateData(GraphmlData data) throws IllegalStateException {
        String key = data.key();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Data key cannot be null or empty.");
        }
    }

    private void validateEdge(IGraphmlEdge edge) throws IllegalStateException {
        String edgeId = edge.id();
        if (edgeId != null && existingEdgeIds.contains(edgeId)) {
            throw new IllegalStateException("Edge must have a unique ID, but id is used several times: '" + edgeId + "'");
        }
        if (edge.directed() == null) throw new IllegalArgumentException("endpoint without direction");
        if (edge.source() == null) throw new IllegalArgumentException("endpoint without sourceId");
        if (edge.target() == null) throw new IllegalArgumentException("endpoint without targetId");
    }

    private void validateGraphMl(IGraphmlDocument document) {
        // always valid
    }

    private void validateHyperEdge(IGraphmlHyperEdge hyperEdge) throws IllegalStateException {
        List<IGraphmlEndpoint> hyperEdgeEndpoints = hyperEdge.endpoints();
        if (hyperEdgeEndpoints == null || hyperEdgeEndpoints.size() < 2)
            throw new IllegalStateException("Hyper edge must have at least 2 endpoints: " + hyperEdge);
    }

    private void validateKey(IGraphmlKey key) throws IllegalStateException {
        String keyId = key.id();
        if (existingKeyIds.contains(keyId)) {
            throw new IllegalStateException("Key ID already exists: " + keyId + ". ID must be unique.");
        } else {
            existingKeyIds.add(keyId);
        }
    }

    private void validateLocator(IGraphmlLocator locator) throws IllegalStateException {
        if (locator == null) return;
        ensureAllowedStart(CurrentElement.LOCATOR);
        ensureAllowedEnd(CurrentElement.LOCATOR);
        if (locator.xlinkHref() == null) throw new IllegalArgumentException("XLinkHref Url cannot be null or empty.");
    }

    private void validateNode(IGraphmlNode node) throws IllegalStateException {
        String nodeId = node.id();
        if (nodeId == null || nodeId.isEmpty()) throw new IllegalStateException("Node must have an ID.");
        if (existingNodeIds.contains(nodeId)) throw new IllegalStateException("Node ID must be unique.");
        validateLocator(node.locator());
    }

}
