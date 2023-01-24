package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.*;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;

import java.io.IOException;


/**
 *
 */
public interface GraphMlWriter {


    void startGraphMl(GioDocument gioGraphML) throws IOException;

    void startKey(GioKey gioKey) throws IOException;

    void end(GioKey gioKey) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    void writeData(XMLValue value) throws IOException;

    void startNode(GioNode node) throws IOException;

    void endNode(GioNode node) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void endEdge(GioEdge gioHyperEdge) throws IOException;

    void endGraph(GioGraph gioGraph) throws IOException;

    void endGraphMl(GioDocument gioGraphML) throws IOException;

}
