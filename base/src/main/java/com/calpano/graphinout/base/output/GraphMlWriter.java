package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.*;

import java.io.IOException;


/**
 *
 */
public interface GraphMlWriter {


    void startGraphMl(GioGraphML gioGraphML) throws IOException;

    void startKey(GioKey gioKey) throws IOException;

    void end(GioKey gioKey) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    void writeData(XMLValue value) throws IOException;

    void startNode(GioNode node) throws IOException;

    void endNode(GioNode node) throws IOException;

    void startEdge(GioHyperEdge edge) throws IOException;

    void endEdge(GioHyperEdge gioHyperEdge) throws IOException;

    void endGraph(GioGraph gioGraph) throws IOException;

    void endGraphMl(GioGraphML gioGraphML) throws IOException;

}
