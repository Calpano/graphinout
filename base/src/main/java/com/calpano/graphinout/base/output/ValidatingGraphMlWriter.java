package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.GraphmlData;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlGraph;
import com.calpano.graphinout.base.graphml.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.GraphmlKey;
import com.calpano.graphinout.base.graphml.GraphmlLocator;
import com.calpano.graphinout.base.graphml.GraphmlNode;
import com.calpano.graphinout.base.graphml.GraphmlWriter;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

public class ValidatingGraphMlWriter implements GraphmlWriter {

    private enum CurrentElement {
        /**
         * state before any token
         */
        EMPTY, GRAPHML, KEY, GRAPH, NODE, EDGE, HYPEREDGE, DESC, DATA, ENDPOINT;

        static {
            // TODO add other nesting rules
            HYPEREDGE.allowedChildren = Set.of(DESC, DATA, ENDPOINT, GRAPH);
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

    public void data(GraphmlKey gioKey) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(gioKey);
        graphMlWriter.data(gioKey);
    }

    @Override
    public void endDocument() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPHML);
        graphMlWriter.endDocument();
    }

    @Override
    public void endEdge(Optional<GraphmlLocator> graphmlLocator) throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
        graphMlWriter.endEdge(graphmlLocator);
    }

    @Override
    public void endGraph() throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPH);
        graphMlWriter.endGraph();
    }

    @Override
    public void endNode(Optional<GraphmlLocator> locator) throws IOException {
        ensureAllowedEnd(CurrentElement.NODE);
        graphMlWriter.endNode(locator);
    }

    @Override
    public void makeEdge(GraphmlHyperEdge edge) throws IOException {
        ensureAllowedStart(CurrentElement.EDGE);
        validateEdge(edge);
        graphMlWriter.makeEdge(edge);
    }

    @Override
    public void makeNode(GraphmlNode node) throws IOException {
        ensureAllowedStart(CurrentElement.NODE);
        validateNode(node);
        graphMlWriter.makeNode(node);
    }

    @Override
    public void startDocument(GraphmlDocument gioGraphML) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
        validateGraphMl(gioGraphML);
        graphMlWriter.startDocument(gioGraphML);
    }

    @Override
    public void startGraph(GraphmlGraph gioGraph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
        // TODO adapt next line once GraphmlModel is simplified
        validateGraph(gioGraph);
        graphMlWriter.startGraph(gioGraph);
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

    private void validateData(GraphmlData gioData) throws IOException {
        String key = gioData.getKey();
        if (key == null || key.isEmpty()) {
            throw new IOException("GraphmlData key cannot be null or empty.");
        }
    }

    private void validateEdge(GraphmlHyperEdge gioEdge) throws IOException {
        if (!gioEdge.getDataList().isEmpty()) {
            for (GraphmlData gioData : gioEdge.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateGraph(GraphmlGraph gioGraph) throws IOException {
        if (!gioGraph.getNodes().isEmpty()) {
            for (GraphmlNode gioNode : gioGraph.getNodes()) {
                validateNode(gioNode);
            }
        }
        if (!gioGraph.getHyperEdges().isEmpty()) {
            for (GraphmlHyperEdge gioEdge : gioGraph.getHyperEdges()) {
                validateEdge(gioEdge);
            }
        }
    }

    private void validateGraphMl(GraphmlDocument gioGraphMl) throws IOException {
        if (!gioGraphMl.getKeys().isEmpty()) {
            for (GraphmlKey gioKey : gioGraphMl.getKeys()) {
                validateKey(gioKey);
            }
        }
        if (!gioGraphMl.getDataList().isEmpty()) {
            for (GraphmlData gioData : gioGraphMl.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateKey(GraphmlKey gioKey) throws IOException {
        if (gioKey.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(gioKey.getId()))) {
            throw new IOException("Key ID already exists: " + gioKey.getId() + ". ID must be unique.");
        }
    }

    private void validateNode(GraphmlNode gioNode) throws IOException {
        if (!gioNode.getDataList().isEmpty()) {
            for (GraphmlData gioData : gioNode.getDataList()) {
                validateData(gioData);
            }
        }
    }
}
