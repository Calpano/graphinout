package com.calpano.graphinout.output;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioData;
import com.calpano.graphinout.graph.GioHyperEdge;
import com.calpano.graphinout.graph.GioNode;
import com.calpano.graphinout.graphml.OutputHandler;

import java.io.IOException;
import java.util.Map;

public class GraphMlWriterToXml<T> implements GraphMlWriter {

    OutputHandler<T> outputHandler;

    @Override
    public void endEdge() {

    }

    @Override
    public void endGraph() {

    }

    @Override
    public void endNode() {

    }

    @Override
    public void startEdge(GioHyperEdge edge) {

    }

    @Override
    public void startGraph(String id, boolean edgeDefaultDirected) throws IOException {
        try {
            outputHandler.startElement("graph", Map.of("edgedefault", edgeDefaultDirected ? "directed" : "undirected"));
        } catch (GioException e) {
            throw new IOException("we can detail the real issue here GIO-123",e);
        }
    }

    @Override
    public void startNode(GioNode node) {

    }

    @Override
    public void writeData(GioData data) {

    }
}
