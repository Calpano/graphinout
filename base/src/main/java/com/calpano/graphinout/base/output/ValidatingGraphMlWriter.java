package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.gio.*;

import java.io.IOException;

public class ValidatingGraphMlWriter implements GraphMlWriter {

    private enum CurrentElement {
        GRAPHML,
        KEY,
        GRAPH,
        NODE,
        EDGE
    }

    private CurrentElement currentElement = CurrentElement.GRAPHML;
    private GraphMlWriter graphMlWriter;

    public ValidatingGraphMlWriter(GraphMlWriter graphMlWriter) {
        this.graphMlWriter = graphMlWriter;
    }

    @Override
    public void startGraphMl(GioDocument gioGraphML) throws IOException {
        if (currentElement != CurrentElement.GRAPHML) {
            throw new IOException("Wrong order of elements, expected GRAPHML but found " + currentElement.name());
        }
        currentElement = CurrentElement.KEY;
        validateGraphMl(gioGraphML);
        graphMlWriter.startGraphMl(gioGraphML);
    }

    @Override
    public void startKey(GioKey gioKey) throws IOException {
        if (currentElement != CurrentElement.KEY) {
            throw new IOException("Wrong order of elements, expected KEY but found " + currentElement.name());
        }
        currentElement = CurrentElement.GRAPH;
        validateKey(gioKey);
        graphMlWriter.startKey(gioKey);
    }

    @Override
    public void end(GioKey gioKey) throws IOException {
        if (currentElement != CurrentElement.GRAPH) {
            throw new IOException("Wrong order of elements, expected GRAPH but found " + currentElement.name());
        }
        graphMlWriter.end(gioKey);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        if (currentElement != CurrentElement.GRAPH) {
            throw new IOException("Wrong order of elements, expected GRAPH but found " + currentElement.name());
        }
        validateGraph(gioGraph);
        currentElement = CurrentElement.NODE;
        graphMlWriter.startGraph(gioGraph);
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        if (currentElement != CurrentElement.NODE) {
            throw new IOException("Wrong order of elements, expected NODE but found " + currentElement.name());
        }
        graphMlWriter.startNode(node);
        validateNode(node);
        graphMlWriter.startNode(node);
    }

    @Override
    public void endNode(GioNode node) throws IOException {
        graphMlWriter.endNode(node);
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        validateEdge(edge);
        graphMlWriter.startEdge(edge);
    }

    @Override
    public void endEdge(GioEdge gioHyperEdge) throws IOException {
        graphMlWriter.endEdge(gioHyperEdge);
    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {
        graphMlWriter.endGraph(gioGraph);
    }

    @Override
    public void endGraphMl(GioDocument gioGraphML) throws IOException {
        graphMlWriter.endGraphMl(gioGraphML);
    }

    private void validateData(GioData gioData) throws IOException {
        String key = gioData.getKey();
        if (key == null || key.isEmpty()) {
            throw new IOException("GioData key cannot be null or empty.");
        }
    }

    private void validateNode(GioNode gioNode) throws IOException {
        if (!gioNode.getDataList().isEmpty()) {
            for (GioData gioData : gioNode.getDataList()) {
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

    private void validateEdge(GioEdge gioEdge) throws IOException {
        if (!gioEdge.getDataList().isEmpty()) {
            for (GioData gioData : gioEdge.getDataList()) {
                validateData(gioData);
            }
        }
    }

    private void validateKey(GioKey gioKey) throws IOException {
        if (gioKey.getDataList().stream().anyMatch(existingKey -> existingKey.getId().equals(gioKey.getId()))) {
            throw new IOException("Key ID already exists: " + gioKey.getId() + ". ID must be unique.");
        }
    }
}
