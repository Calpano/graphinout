package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ValidatingGraphMlWriter implements GraphMlWriter {

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
    private final GraphMlWriter graphMlWriter;

    public ValidatingGraphMlWriter(GraphMlWriter graphMlWriter) {
        this.currentElements = new LinkedList<>();
        // never deal with an empty stack
        currentElements.push(CurrentElement.EMPTY);
        this.graphMlWriter = graphMlWriter;
    }

    @Override
    public void end(GioKey gioKey) throws IOException {
        ensureAllowedEnd(CurrentElement.KEY);
        graphMlWriter.end(gioKey);
    }

    @Override
    public void endEdge(GioEdge edge) throws IOException {
        ensureAllowedEnd(CurrentElement.EDGE);
        graphMlWriter.endEdge(edge);
    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPH);
        graphMlWriter.endGraph(gioGraph);
    }

    @Override
    public void endGraphMl(GioDocument gioGraphML) throws IOException {
        ensureAllowedEnd(CurrentElement.GRAPHML);
        graphMlWriter.endGraphMl(gioGraphML);
    }

    @Override
    public void endNode(GioNode node) throws IOException {
        ensureAllowedEnd(CurrentElement.NODE);
        graphMlWriter.endNode(node);
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        ensureAllowedStart(CurrentElement.EDGE);
        validateEdge(edge);
        graphMlWriter.startEdge(edge);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPH);
        // TODO adapt next line once GioModel is simplified
        validateGraph(gioGraph);
        graphMlWriter.startGraph(gioGraph);
    }

    @Override
    public void startGraphMl(GioDocument gioGraphML) throws IOException {
        ensureAllowedStart(CurrentElement.GRAPHML);
        validateGraphMl(gioGraphML);
        graphMlWriter.startGraphMl(gioGraphML);
    }

    @Override
    public void startKey(GioKey gioKey) throws IOException {
        ensureAllowedStart(CurrentElement.KEY);
        validateKey(gioKey);
        graphMlWriter.startKey(gioKey);
    }

    @Override
    public void startNode(GioNode node) throws IOException {
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

    private void validateData(GioData gioData) throws IOException {
        String key = gioData.getKey();
        if (key == null || key.isEmpty()) {
            throw new IOException("GioData key cannot be null or empty.");
        }
    }

    private void validateEdge(GioEdge gioEdge) throws IOException {
        if (!gioEdge.getDataList().isEmpty()) {
            for (GioData gioData : gioEdge.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateGraph(GioGraph gioGraph) throws IOException {
        if (!gioGraph.getNodes().isEmpty()) {
            for (GioNode gioNode : gioGraph.getNodes()) {
                validateNode(gioNode);
            }
        }
        if (!gioGraph.getHyperEdges().isEmpty()) {
            for (GioEdge gioEdge : gioGraph.getHyperEdges()) {
                validateEdge(gioEdge);
            }
        }
    }

    private void validateGraphMl(GioDocument gioGraphMl) throws IOException {
        if (!gioGraphMl.getKeys().isEmpty()) {
            for (GioKey gioKey : gioGraphMl.getKeys()) {
                validateKey(gioKey);
            }
        }
        if (!gioGraphMl.getDataList().isEmpty()) {
            for (GioData gioData : gioGraphMl.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateKey(GioKey gioKey) throws IOException {
        if (gioKey.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(gioKey.getId()))) {
            throw new IOException("Key ID already exists: " + gioKey.getId() + ". ID must be unique.");
        }
    }

    private void validateNode(GioNode gioNode) throws IOException {
        if (!gioNode.getDataList().isEmpty()) {
            for (GioData gioData : gioNode.getDataList()) {
                validateData(gioData);
            }
        }
    }
}
