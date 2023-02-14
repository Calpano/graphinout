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
    private final Set<String> edgeReferences = new HashSet<>();

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
        resolveEdges(existingNodeIds, existingEdgeIds, edgeReferences);
        graphMlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
        // check edge, if all endpoint nodes are already defined => fine
        // if not => remember edge references (uses costly memory :-)
        graphMlWriter.endEdge();
    }

    @Override
    public void endGraph(Optional<GraphmlLocator> locator) throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPH);
        // validate all open references here
        graphMlWriter.endGraph(locator);
    }

    @Override
    public void endHyperEdge(Optional<GraphmlLocator> locator) throws IOException {
        ensureAllowedEnd(CurrentElement.HYPEREDGE);
        graphMlWriter.endHyperEdge(locator);
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
        existingEdgeIds.add(edge.getId());
        validateEdge(edge);
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
        graphMlWriter.startHyperEdge(hyperEdge);
    }

    @Override
    public void startNode(GraphmlNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
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
            throw new IllegalStateException("Wrong order of elements, expected one of " + currentElement.allowedChildren + " but found " + currentElement);
        }
        currentElements.push(childElement);
    }

    private void resolveEdges(Set<String> nodeIds, Set<String> usedEdgeIds, Set<String> edgesReferences) throws IOException {
        for (String edgeRef : edgesReferences) {
            if (!usedEdgeIds.contains(edgeRef)) {
                throw new IOException("Edge refers to a non-existing edge ID: " + edgeRef);
            }
        }
    }

    private void validateData(GraphmlData data) throws IOException {
        String key = data.getKey();
        if (key == null || key.isEmpty()) {
            throw new IOException("Data key cannot be null or empty.");
        }
    }

    private void validateEdge(GraphmlEdge edge) throws IOException {
        String sourceId = edge.getSource().getId();
        String targetId = edge.getTarget().getId();
        String edgeId = edge.getId();
        if (sourceId == null || targetId == null || sourceId.isEmpty() || targetId.isEmpty() ||
                !existingNodeIds.contains(sourceId) || !existingNodeIds.contains(targetId)) {
            throw new IOException("Edge must have a source and target that refers to a node.");
        }
        if (edgeId == null || edgeId.isEmpty() || existingEdgeIds.contains(edgeId)) {
            throw new IOException("Edge must have a unique ID.");
        }
        if (!edge.getData().isEmpty()) {
            for (GraphmlData gioData : edge.getData()) {
                validateData(gioData);
            }
        }
    }

    private void validateHyperEdge(GraphmlHyperEdge hyperEdge) throws IOException {
        if (!hyperEdge.getDataList().isEmpty()) {
            for (GraphmlData gioData : hyperEdge.getDataList()) {
                validateData(gioData);
            }
        }
        List<GraphmlEndpoint> endpoints = hyperEdge.getEndpoints();
        if (endpoints == null || endpoints.size() < 2) {
            throw new IOException("Hyper edge must have at least 2 endpoints.");
        }
    }

    private void validateGraph(GraphmlGraph graph) throws IOException {
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
        if (!document.getKeys().isEmpty()) {
            for (GraphmlKey gioKey : document.getKeys()) {
                validateKey(gioKey);
            }
        }
        if (!document.getDataList().isEmpty()) {
            for (GraphmlData gioData : document.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateKey(GraphmlKey key) throws IOException {
        if (key.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(key.getId()))) {
            throw new IOException("Key ID already exists: " + key.getId() + ". ID must be unique.");
        }
    }

    private void validateNode(GraphmlNode node) throws IOException {
        if (!node.getDataList().isEmpty()) {
            for (GraphmlData gioData : node.getDataList()) {
                validateData(gioData);
            }
        }
    }
}
