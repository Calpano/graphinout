package com.calpano.graphinout.base.gio;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter {

    void data(GioData data) throws IOException;

    /**
     * Convenience method to add data
     *
     * @param key
     * @param data
     * @throws IOException
     */
    default void data(String key, String data) throws IOException {
        data(GioData.builder().key(key).value(data).build());
    }

    void endDocument() throws IOException;

    void endEdge(Optional<URL> locator) throws IOException;

    void endGraph(Optional<URL> locator) throws IOException;

    void endNode(Optional<URL> locator) throws IOException;

    void endPort();

    void startDocument(GioDocument document) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    /**
     * May contain #startGraph -- DTD is a bit unclear here whether 1 or multiple graphs are allowed. 1 seems more plausible.
     */
    void startNode(GioNode node) throws IOException;

    void startPort(GioPort port);

}
