package com.calpano.graphinout.base.gio;

import java.io.IOException;
import java.util.Optional;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter {

    void startDocument(GioDocument document) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    void data(GioKey data) throws IOException;


    /**
     * May contain #startGraph -- DTD is a bit unclear here whether 1 or multiple graphs are allowed. 1 seems more plausible.
     */
    void startNode(GioNode node) throws IOException;

    void endNode(Optional<GioLocator> locator) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void endEdge(Optional<GioLocator> locator) throws IOException;

    void endGraph() throws IOException;

   void endDocument() throws IOException;

}
