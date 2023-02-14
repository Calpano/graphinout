package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;

import java.io.IOException;
import java.util.*;

public class ValidatingGraphMlWriter implements GraphmlWriter {

    private enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, HYPEREDGE, DESC, DATA, ENDPOINT, EDGE, DEFAULT, PORT, LABEL_GRAPH,
        LABEL_NODE, LABEL_EDGE, LABEL_HYPEREDGE, LABEL_ENDPOINT;

        static {
            EMPTY.allowedChildren = Set.of(GRAPHML);
            GRAPHML.allowedChildren = Set.of(KEY, DATA, GRAPH, DESC, LABEL_GRAPH);
            KEY.allowedChildren = Set.of(DESC, DEFAULT, LABEL_GRAPH);
            DEFAULT.allowedChildren = Set.of(DATA, LABEL_GRAPH);
            GRAPH.allowedChildren = Set.of(DATA, NODE, EDGE, HYPEREDGE, PORT, DESC, LABEL_GRAPH);
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
        validateEdge(hyperEdge);
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

    private void validateData(GraphmlData data) throws IOException {
        String key = data.getKey();
        if (key == null || key.isEmpty()) {
            throw new IOException("GraphmlData key cannot be null or empty.");
        }
    }

    private void validateEdge(GraphmlEdge edge) throws IOException {
        if (edge.getSource() == null || edge.getTarget() == null) {
            throw new IOException("Edge must have a source and a target.");
        }
        if (!edge.getData().isEmpty()) {
            for (GraphmlData gioData : edge.getData()) {
                validateData(gioData);
            }
        }
    }

    private void validateEdge(GraphmlHyperEdge hyperEdge) throws IOException {
        if (!hyperEdge.getDataList().isEmpty()) {
            for (GraphmlData gioData : hyperEdge.getDataList()) {
                validateData(gioData);
            }
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
                validateEdge(gioEdge);
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
