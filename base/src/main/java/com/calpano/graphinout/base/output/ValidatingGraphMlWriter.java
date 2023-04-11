package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;
import org.slf4j.Logger;

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
    private final GraphmlWriter graphMlWriter;
    private final Set<String> existingNodeIds = new HashSet<>();
    private final Set<String> existingEdgeIds = new HashSet<>();

    private final Map<String, List<String>> nonExistingNode = new HashMap<>();

    public ValidatingGraphMlWriter(GraphmlWriter graphMlWriter) {
        this.currentElements = new LinkedList<>();
        // never deal with an empty stack
        currentElements.push(CurrentElement.EMPTY);
        this.graphMlWriter = graphMlWriter;
    }

    @Override
    public void data(GraphmlData data) throws IOException {
        ensureAllowedStart(CurrentElement.DATA);
        ensureAllowedEnd(CurrentElement.DATA);
        if (data.getKey() == null) {
            throw new IllegalArgumentException("Graphml data must have a key attribute.");
        }
        graphMlWriter.data(data);
    }

    @Override
    public void endDocument() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPHML);
        if (!nonExistingNode.isEmpty()) {
            nonExistingNode.forEach((s, messages) -> messages.forEach(log::warn));
            throw new IllegalStateException(nonExistingNode.size() + " nodes used in the graph without reference.");
        }
        graphMlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
        graphMlWriter.endEdge();
    }

    @Override
    public void endGraph(Optional<GraphmlLocator> locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.GRAPH);
        graphMlWriter.endGraph(locator);
    }

    @Override
    public void endHyperEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.HYPEREDGE);
        graphMlWriter.endHyperEdge();
    }

    @Override
    public void endNode(Optional<GraphmlLocator> locator) throws IOException {
        validateLocator(locator);
        ensureAllowedEnd(CurrentElement.NODE);
        graphMlWriter.endNode(locator);
    }

    @Override
    public void endPort() throws IOException {
        ensureAllowedEnd(CurrentElement.PORT);
        graphMlWriter.endPort();
    }

    public void key(GraphmlKey key) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(key);
        graphMlWriter.key(key);
        ensureAllowedEnd(CurrentElement.KEY);
    }

    @Override
    public void startDocument(GraphmlDocument document) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
        validateGraphMl(document);
        graphMlWriter.startDocument(document);
    }

    @Override
    public void startEdge(GraphmlEdge edge) throws IOException {
        ensureAllowedStart(CurrentElement.EDGE);
        validateEdge(edge);
        investigatingTheExistenceOfTheNode(edge);
        if (edge.getId() != null) existingEdgeIds.add(edge.getId());
        graphMlWriter.startEdge(edge);
    }

    @Override
    public void startGraph(GraphmlGraph graph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
        // TODO adapt next line once GraphmlModel is simplified
        validateGraph(graph);
        graphMlWriter.startGraph(graph);
    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge hyperEdge) throws IOException {
        ensureAllowedStart(CurrentElement.HYPEREDGE);
        validateHyperEdge(hyperEdge);
        investigatingTheExistenceOfTheNode(hyperEdge);
        existingEdgeIds.add(hyperEdge.getId());
        graphMlWriter.startHyperEdge(hyperEdge);
    }

    @Override
    public void startNode(GraphmlNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
        existingNodeIds.add(node.getId());
        graphMlWriter.startNode(node);
    }

    @Override
    public void startPort(GraphmlPort port) throws IOException {
        ensureAllowedStart(CurrentElement.PORT);
        // TODO implement validation #90
        graphMlWriter.startPort(port);
    }

    private void ensureAllowedEnd(CurrentElement element) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        if (currentElement != element) {
            throw new IllegalStateException("Wrong order of calls. Cannot END '" + element + "', last started element was " + currentElement);
        }
        currentElements.pop();
        log.debug("ENDED '" + element.name() + "'. Stack: " + stackToString());
    }

    private void ensureAllowedStart(CurrentElement childElement) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        assert currentElement != null;
        if (!currentElement.isValidChild(childElement)) {
            throw new IllegalStateException("Wrong order of elements. In element " + currentElement + " expected one of " + currentElement.allowedChildren + " but found " + childElement + ". Stack (leaf-to-root): " + this.currentElements);
        }
        currentElements.push(childElement);
        log.debug("STARTED '" + childElement.name() + "' => Stack: " + stackToString());
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
        if (edge.getDataList() != null) {
            for (GraphmlData gioData : edge.getDataList()) {
                validateData(gioData);
            }
        }
        if (edge.getDirected() == null) throw new IllegalArgumentException("endpoint without direction");
        if (edge.getSourceId() == null) throw new IllegalArgumentException("endpoint without sourceId");
        if (edge.getTargetId() == null) throw new IllegalArgumentException("endpoint without targetId");
    }

    private void validateGraph(GraphmlGraph graph) throws IllegalStateException {
        if (!graph.getNodes().isEmpty()) {
            for (GraphmlNode gioNode : graph.getNodes()) {
                validateNode(gioNode);
            }
        }
        if (!graph.getHyperEdges().isEmpty()) {
            for (GraphmlHyperEdge gioEdge : graph.getHyperEdges()) {
                validateHyperEdge(gioEdge);
            }
        }
    }

    private void validateGraphMl(GraphmlDocument document) {
        if (document.getKeys() != null) {
            for (GraphmlKey gioKey : document.getKeys()) {
                validateKey(gioKey);
            }
        }
        if (document.getDataList() != null) {
            for (GraphmlData gioData : document.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateHyperEdge(GraphmlHyperEdge hyperEdge) throws IllegalStateException {
        if (hyperEdge.getDataList() != null) {
            for (GraphmlData gioData : hyperEdge.getDataList()) {
                validateData(gioData);
            }
        }
        List<GraphmlEndpoint> hyperEdgeEndpoints = hyperEdge.getEndpoints();
        if (hyperEdgeEndpoints == null || hyperEdgeEndpoints.size() < 2)
            throw new IllegalStateException("Hyper edge must have at least 2 endpoints: " + hyperEdge);
    }

    private void validateKey(GraphmlKey key) throws IllegalStateException {
        if (key.getDataList() != null && key.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(key.getId()))) {
            throw new IllegalStateException("Key ID already exists: " + key.getId() + ". ID must be unique.");
        }
    }

    private void validateNode(GraphmlNode node) throws IllegalStateException {
        if (node.getDataList() != null) {
            for (GraphmlData gioData : node.getDataList()) {
                validateData(gioData);
            }
        }
        String nodeId = node.getId();
        if (nodeId == null || nodeId.isEmpty()) throw new IllegalStateException("Node must have an ID.");
        if (existingNodeIds.contains(nodeId)) throw new IllegalStateException("Node ID must be unique.");
    }

    private void validateLocator(Optional<GraphmlLocator> locator) throws IllegalStateException {
        if (locator.isEmpty()) return;
        GraphmlLocator graphmlLocator = locator.get();
        ensureAllowedStart(CurrentElement.LOCATOR);
        ensureAllowedEnd(CurrentElement.LOCATOR);
        if (graphmlLocator.getXLinkHref() == null)
            throw new IllegalArgumentException("XLinkHref Url cannot be null or empty.");
    }

    private enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, HYPEREDGE, DESC, DATA, EDGE, PORT, LOCATOR, DEFAULT, ENDPOINT;

        static {
            EMPTY.allowedChildren = Set.of(GRAPHML);
            GRAPHML.allowedChildren = Set.of(DATA, DESC, KEY, GRAPH);
            KEY.allowedChildren = Set.of(DESC, DEFAULT);
            NODE.allowedChildren = Set.of(DATA, DESC, GRAPH, LOCATOR, PORT);
            GRAPH.allowedChildren = Set.of(DATA, DESC, NODE, EDGE, HYPEREDGE, LOCATOR);
            EDGE.allowedChildren = Set.of(DATA, DESC, GRAPH);
            HYPEREDGE.allowedChildren = Set.of(DATA, DESC, ENDPOINT);
            PORT.allowedChildren = Set.of(DATA, PORT);
        }

        private Set<CurrentElement> allowedChildren = new HashSet<>();

        public boolean isValidChild(CurrentElement childElement) {
            return allowedChildren.contains(childElement);
        }
    }
}
