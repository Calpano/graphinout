package com.calpano.graphinout.base.graphml;

import java.io.IOException;
import java.util.Optional;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GraphmlWriter {

    void key(GraphmlKey data) throws IOException;

    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(Optional<GraphmlLocator> graphmlLocator) throws IOException;

    void endHyperEdge() throws IOException;

    void endNode(Optional<GraphmlLocator> locator) throws IOException;

    void startDocument(GraphmlDocument document) throws IOException;

    void startEdge(GraphmlEdge edge) throws IOException;

    void startGraph(GraphmlGraph graphmlGraph) throws IOException;

    void startHyperEdge(GraphmlHyperEdge edge) throws IOException;

    void startNode(GraphmlNode node) throws IOException;

    void data(GraphmlData data) throws IOException;

    void startPort(GraphmlPort port) throws IOException;
    void endPort() throws IOException;


}
