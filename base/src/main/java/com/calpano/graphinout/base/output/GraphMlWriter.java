package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.GioData;
import com.calpano.graphinout.base.GioHyperEdge;
import com.calpano.graphinout.base.GioNode;

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
