package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlKey;
import com.calpano.graphinout.base.graphml.GraphmlNode;

import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter {

    void data(GioKey data) throws IOException;

    void endDocument() throws IOException;

    void endGraph() throws IOException;

    void startDocument(GioDocument document) throws IOException;

    void startGraph() throws IOException;

    /**
     * May contain #startGraph
     * @param node
     * @throws IOException
     */
    void startNode(GioNode node) throws IOException;

}
