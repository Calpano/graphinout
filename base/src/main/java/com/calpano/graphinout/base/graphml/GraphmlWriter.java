package com.calpano.graphinout.base.graphml;

import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GraphmlWriter {

    void data(GraphmlKey data) throws IOException;

    void endDocument() throws IOException;

    void endGraph() throws IOException;

    void startDocument(GraphmlDocument document) throws IOException;

    void startGraph() throws IOException;

    void startNode(GraphmlNode node) throws IOException;

}
