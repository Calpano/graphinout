package com.calpano.graphinout.base.gio;

import javax.annotation.Detainted;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter {


    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(@Nullable URL locator) throws IOException;

    void endNode(@Nullable URL locator) throws IOException;

    void key(GioKey gioKey) throws IOException;

    void data(GioData data) throws IOException;

    void startDocument(GioDocument document) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    /**
     * May contain #startGraph -- DTD is a bit unclear here whether 1 or multiple graphs are allowed. 1 seems more plausible.
     */
    void startNode(GioNode node) throws IOException;

    void startPort(GioPort port) throws IOException;
    void endPort() throws IOException;


}
