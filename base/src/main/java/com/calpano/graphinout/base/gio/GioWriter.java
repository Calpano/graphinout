package com.calpano.graphinout.base.gio;

import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter {

    void startDocument(GioDocument document) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    void data(GioKey data) throws IOException;


    /**
     * May contain #startGraph
     * @param node
     * @throws IOException
     */
    void startNode(GioNode node) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void endGraph() throws IOException;

   void endDocument() throws IOException;

}
