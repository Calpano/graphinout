package com.calpano.graphinout.output;

import com.calpano.graphinout.graph.Direction;
import com.calpano.graphinout.graph.GioData;
import com.calpano.graphinout.graph.GioHyperEdge;
import com.calpano.graphinout.graph.GioNode;

import java.io.IOException;


/**
 *
 */
public interface GraphMlWriter {

    /**
     * @param id
     * @param edgeDefaultDirected false = undirected graph
     */
    void startGraph(String id, boolean edgeDefaultDirected) throws IOException;

    void writeData(GioData data) throws IOException;

    void startNode(GioNode node) throws IOException;
    void endNode() throws IOException;

    void startEdge(GioHyperEdge edge) throws IOException;
    void endEdge() throws IOException;

    void endGraph() throws IOException;

}
