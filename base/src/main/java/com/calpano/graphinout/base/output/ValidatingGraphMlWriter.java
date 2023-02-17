package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;

import java.io.IOException;
import java.util.*;

public class ValidatingGraphMlWriter implements GraphmlWriter {

    private enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, HYPEREDGE, DESC, DATA, ENDPOINT, EDGE, DEFAULT, PORT, LOCATOR, XLINKHREF,
        LABEL_GRAPH, LABEL_NODE, LABEL_EDGE, LABEL_HYPEREDGE, LABEL_ENDPOINT;

        static {
            EMPTY.allowedChildren = Set.of(GRAPHML);
            GRAPHML.allowedChildren = Set.of(KEY, DATA, GRAPH, DESC, LABEL_GRAPH);
            KEY.allowedChildren = Set.of(DESC, DEFAULT, LABEL_GRAPH);
            DEFAULT.allowedChildren = Set.of(DATA, LABEL_GRAPH);
            GRAPH.allowedChildren = Set.of(DATA, NODE, EDGE, HYPEREDGE, PORT, DESC, LOCATOR, LABEL_GRAPH);
            LOCATOR.allowedChildren = Set.of(XLINKHREF);
            NODE.allowedChildren = Set.of(DATA, PORT, DESC, LABEL_NODE);
            EDGE.allowedChildren = Set.of(DATA, ENDPOINT, PORT, DESC, LABEL_EDGE);
            HYPEREDGE.allowedChildren = Set.of(DATA, PORT, DESC, LABEL_HYPEREDGE);
            ENDPOINT.allowedChildren = Set.of(DATA, PORT, DESC, LABEL_ENDPOINT);
            PORT.allowedChildren = Set.of(DATA, LABEL_GRAPH, LABEL_NODE, LABEL_EDGE, LABEL_HYPEREDGE, LABEL_ENDPOINT);
        }

        private Set<CurrentElement> allowedChildren = new HashSet<>();

        public boolean isValidChild(CurrentElement childElement) {
            return allowedChildren.contains(childElement);
        }
    }

    /**
     * remember elements we saw already; nesting order in stack
     */
    private final Deque<CurrentElement> currentElements;
    private final GraphmlWriter graphMlWriter;
    private final Set<String> existingNodeIds = new HashSet<>();
    private final Set<String> existingEdgeIds = new HashSet<>();
    private final Set<GraphmlEdge> edgeReferences = new HashSet<>();
    private final Set<GraphmlHyperEdge> hyperEdgeReferences = new HashSet<>();

    public ValidatingGraphMlWriter(GraphmlWriter graphMlWriter) {
        this.currentElements = new LinkedList<>();
        // never deal with an empty stack
        currentElements.push(CurrentElement.EMPTY);
        this.graphMlWriter = graphMlWriter;
    }

    public void data(GraphmlKey key) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(key);
        graphMlWriter.data(key);
    }

    @Override
    public void endDocument() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPHML);
        for (GraphmlEdge edge : edgeReferences) {
            if (!resolveEdges(edge)) throw new IllegalStateException("Edge references to a non-existent node ID");
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
        ensureAllowedEnd(CurrentElement.GRAPH);
        // TODO validate all open edgeReferences here
        graphMlWriter.endGraph(locator);
    }

    @Override
    public void endHyperEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.HYPEREDGE);
        graphMlWriter.endHyperEdge();
    }

    @Override
    public void endNode(Optional<GraphmlLocator> locator) throws IOException {
        ensureAllowedEnd(CurrentElement.NODE);
        // TODO call validation checks
        graphMlWriter.endNode(locator);
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
        existingEdgeIds.add(edge.getId());
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

    private void ensureAllowedEnd(CurrentElement element) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        if (currentElement != element) {
            throw new IllegalStateException("Wrong order of calls. Cannot END '" + element + "', last started element was " + currentElement);
        }
        currentElements.pop();
    }

    private void ensureAllowedStart(CurrentElement childElement) throws IllegalStateException {
        CurrentElement currentElement = currentElements.peek();
        if (!currentElement.isValidChild(childElement)) {
            throw new IllegalStateException("Wrong order of elements. In element "+currentElement
                    +" expected one of " + currentElement.allowedChildren + " but found " + childElement);
        }
        currentElements.push(childElement);
    }

    private boolean resolveEdges(GraphmlEdge edge) {
        String sourceId = edge.getSource().getId();
        String targetId = edge.getTarget().getId();
        if (!existingNodeIds.contains(sourceId)) {
            // IMPROVE remember just just the undefined nodeId? remember whole edge: better error reporting
            edgeReferences.add(edge);
            return false;
        }
        if (!existingNodeIds.contains(targetId)) {
            edgeReferences.add(edge);
            return false;
        }
        return true;
    }

    private boolean resolveHyperEdges(GraphmlHyperEdge hyperEdge) {
        List<GraphmlEndpoint> endpoints = hyperEdge.getEndpoints();
        for (GraphmlEndpoint endpoint : endpoints) {
            if (!existingNodeIds.contains(endpoint.getNode())) {
                hyperEdgeReferences.add(hyperEdge);
                return false;
            }
        }
        return true;
    }

    private void validateData(GraphmlData data) throws IllegalStateException {
        String key = data.getKey();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Data key cannot be null or empty.");
        }
    }

    private void validateEdge(GraphmlEdge edge) throws IllegalStateException {
        String edgeId = edge.getId();
        // TODO verify: edges must have id?
        if (edgeId == null || edgeId.isEmpty() || existingEdgeIds.contains(edgeId)) {
            throw new IllegalStateException("Edge must have a unique ID.");
        }
        if (!edge.getData().isEmpty()) {
            for (GraphmlData gioData : edge.getData()) {
                validateData(gioData);
            }
        }
    }

    private void validateHyperEdge(GraphmlHyperEdge hyperEdge) throws IllegalStateException {
        if (hyperEdge.getDataList()!=null) {
            for (GraphmlData gioData : hyperEdge.getDataList()) {
                validateData(gioData);
            }
        }
        List<GraphmlEndpoint> hyperEdgeEndpoints = hyperEdge.getEndpoints();
        // id is optional
        if (hyperEdgeEndpoints == null || hyperEdgeEndpoints.size() < 2)
            throw new IllegalStateException("Hyper edge must have at least 2 endpoints: "+hyperEdge);
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

    private void validateGraphMl(GraphmlDocument document) throws IOException {
        if (document.getKeys()!=null) {
            for (GraphmlKey gioKey : document.getKeys()) {
                validateKey(gioKey);
            }
        }
        if (document.getDataList()!=null) {
            for (GraphmlData gioData : document.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateKey(GraphmlKey key) throws IllegalStateException {
        if (key.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(key.getId()))) {
            throw new IllegalStateException("Key ID already exists: " + key.getId() + ". ID must be unique.");
        }
    }

    private void validateNode(GraphmlNode node) throws IllegalStateException {
        if (!node.getDataList().isEmpty()) {
            for (GraphmlData gioData : node.getDataList()) {
                validateData(gioData);
            }
        }
        String nodeId = node.getId();
        if (nodeId == null || nodeId.isEmpty()) {
            throw new IllegalStateException("Node must have an ID.");
        }
    }
}
