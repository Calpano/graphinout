package com.calpano.graphinout.base.validation;

import com.calpano.graphinout.base.graphml.*;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class ValidatingGraphMlWriter implements GraphmlWriter {

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
    public void data(GraphmlData data) throws IOException {
        ensureAllowedStart(CurrentElement.DATA);
        ensureAllowedEnd(CurrentElement.DATA);
        if (data.getKey() == null) {
            throw new IllegalArgumentException("Graphml data must have a key attribute.");
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
    public void endGraph(@Nullable GraphmlLocator locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.GRAPH);
    }

    @Override
    public void endHyperEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.HYPEREDGE);
    }

    @Override
    public void endNode(@Nullable GraphmlLocator locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.NODE);
    }

    @Override
    public void endPort() throws IOException {
        ensureAllowedEnd(CurrentElement.PORT);
    }

    public void key(GraphmlKey key) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(key);
        ensureAllowedEnd(CurrentElement.KEY);
    }

    @Override
    public void startDocument(GraphmlDocument document) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
        validateGraphMl(document);
    }

    @Override
    public void startEdge(GraphmlEdge edge) throws IOException {
        ensureAllowedStart(CurrentElement.EDGE);
        validateEdge(edge);
        investigatingTheExistenceOfTheNode(edge);
        if (edge.getId() != null) existingEdgeIds.add(edge.getId());

        investigatingTheExistenceOfThePort(edge);
    }

    @Override
    public void startGraph(GraphmlGraph graph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge hyperEdge) throws IOException {
        ensureAllowedStart(CurrentElement.HYPEREDGE);
        validateHyperEdge(hyperEdge);
        investigatingTheExistenceOfTheNode(hyperEdge);
        investigatingTheExistenceOfThePort(hyperEdge);
        existingEdgeIds.add(hyperEdge.getId());
    }

    @Override
    public void startNode(GraphmlNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
        existingNodeIds.add(node.getId());
        nonExistingNode.remove(node.getId());
    }

    @Override
    public void startPort(GraphmlPort port) throws IOException {
        // FIXME port names are not unique per graph, but only unique within a node
        ensureAllowedStart(CurrentElement.PORT);
        if (port.getName() == null || port.getName().trim().isEmpty())
            throw new IllegalStateException("Name of Port cannot be null or empty.");

        if (existingPortNames.contains(port.getName()))
            throw new IllegalStateException("Port must have a unique Name, but name is used several times: '" + port.getName() + "'.");
        existingPortNames.add(port.getName());
        nonExistingPortNames.remove(port.getName() );
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
    private void investigatingTheExistenceOfTheNode(GraphmlEdge edge) throws IllegalStateException {
        for (String nodeId : Arrays.asList(edge.getSourceId(), edge.getTargetId())) {
            if (!existingNodeIds.contains(nodeId)) {
                nonExistingNode.computeIfAbsent(nodeId, key -> new ArrayList<>()) //
                        .add("Edge [" + edge + "] references to a non-existent node ID: '" + nodeId + "'");
            } else nonExistingNode.remove(nodeId);
        }
    }

    private void investigatingTheExistenceOfThePort(@Nonnull GraphmlEdge edge) throws IllegalStateException {
        for (String portName : Arrays.asList(edge.getSourcePortId(), edge.getTargetPortId())) {
            if (portName != null) if (!existingPortNames.contains(portName)) {
                nonExistingPortNames.computeIfAbsent(portName, key -> new ArrayList<>()) //
                        .add("Edge [" + edge + "] references to a non-existent port Name: '" + portName + "'");
            } else nonExistingNode.remove(portName);
        }
    }

    private void investigatingTheExistenceOfThePort(@Nonnull GraphmlHyperEdge hyperEdge) throws IllegalStateException {
        for (GraphmlEndpoint endpoint : hyperEdge.getEndpoints()) {
            if (endpoint.getPort() != null) if (!existingPortNames.contains(endpoint.getPort())) {
                nonExistingPortNames.computeIfAbsent(endpoint.getPort(), key -> new ArrayList<>()) //
                        .add("Hyper Edge [" + hyperEdge + "] references to a non-existent port Name: '" + endpoint.getPort() + "'");
            } else nonExistingNode.remove(endpoint.getPort());
        }
    }

    /**
     * @param hyperEdge
     */
    private void investigatingTheExistenceOfTheNode(GraphmlHyperEdge hyperEdge) throws IllegalStateException {
        List<GraphmlEndpoint> endpoints = hyperEdge.getEndpoints();
        for (GraphmlEndpoint endpoint : endpoints) {

            if (!existingNodeIds.contains(endpoint.getNode())) {
                String nodeId = endpoint.getNode();
                nonExistingNode.computeIfAbsent(nodeId, key -> new ArrayList<>()) //
                        .add("Hyper Edge [" + hyperEdge + "] references to a non-existent node ID: '" + nodeId + "'");
            } else nonExistingNode.remove(endpoint.getNode());
        }
    }

    private String stackToString() {
        List<String> list = currentElements.stream().map(Enum::name).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(list);
        return String.join("|", list);
    }

    private void validateData(GraphmlData data) throws IllegalStateException {
        String key = data.getKey();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Data key cannot be null or empty.");
        }
    }

    private void validateEdge(GraphmlEdge edge) throws IllegalStateException {
        String edgeId = edge.getId();
        if (edgeId != null && existingEdgeIds.contains(edgeId)) {
            throw new IllegalStateException("Edge must have a unique ID, but id is used several times: '" + edgeId + "'");
        }
        if (edge.getDirected() == null) throw new IllegalArgumentException("endpoint without direction");
        if (edge.getSourceId() == null) throw new IllegalArgumentException("endpoint without sourceId");
        if (edge.getTargetId() == null) throw new IllegalArgumentException("endpoint without targetId");
    }

    private void validateGraphMl(GraphmlDocument document) {
        if (document.getKeys() != null) {
            for (GraphmlKey gioKey : document.getKeys()) {
                validateKey(gioKey);
            }
        }
    }

    private void validateHyperEdge(GraphmlHyperEdge hyperEdge) throws IllegalStateException {
        List<GraphmlEndpoint> hyperEdgeEndpoints = hyperEdge.getEndpoints();
        if (hyperEdgeEndpoints == null || hyperEdgeEndpoints.size() < 2)
            throw new IllegalStateException("Hyper edge must have at least 2 endpoints: " + hyperEdge);
    }

    private void validateKey(GraphmlKey key) throws IllegalStateException {
        String keyId = key.getId();
        if (existingKeyIds.contains(keyId)) {
            throw new IllegalStateException("Key ID already exists: " + keyId + ". ID must be unique.");
        } else {
            existingKeyIds.add(keyId);
        }
    }

    private void validateLocator(GraphmlLocator locator) throws IllegalStateException {
        if(locator==null) return;
        ensureAllowedStart(CurrentElement.LOCATOR);
        ensureAllowedEnd(CurrentElement.LOCATOR);
        if (locator.getXLinkHref() == null)
            throw new IllegalArgumentException("XLinkHref Url cannot be null or empty.");
    }

    private void validateNode(GraphmlNode node) throws IllegalStateException {
        String nodeId = node.getId();
        if (nodeId == null || nodeId.isEmpty()) throw new IllegalStateException("Node must have an ID.");
        if (existingNodeIds.contains(nodeId)) throw new IllegalStateException("Node ID must be unique.");
    }

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
}
