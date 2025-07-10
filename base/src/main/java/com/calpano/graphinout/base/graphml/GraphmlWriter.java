package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GraphmlWriter {

    void data(GraphmlData data) throws IOException;

    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(@Nullable GraphmlLocator graphmlLocator) throws IOException;

    void endHyperEdge() throws IOException;

    void endNode(@Nullable GraphmlLocator locator) throws IOException;

    void endPort() throws IOException;

    void key(GraphmlKey data) throws IOException;

    void startDocument(GraphmlDocument document) throws IOException;

    void startEdge(GraphmlEdge edge) throws IOException;

    void startGraph(GraphmlGraph graphmlGraph) throws IOException;

    void startHyperEdge(GraphmlHyperEdge edge) throws IOException;

    void startNode(GraphmlNode node) throws IOException;

    void startPort(GraphmlPort port) throws IOException;


}
