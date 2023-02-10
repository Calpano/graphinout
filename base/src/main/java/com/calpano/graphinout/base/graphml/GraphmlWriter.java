package com.calpano.graphinout.base.graphml;

import java.io.IOException;
import java.util.Optional;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GraphmlWriter {

    void data(GraphmlKey data) throws IOException;

    void endDocument() throws IOException;

    void endGraph() throws IOException;

    void startDocument(GraphmlDocument document) throws IOException;

    void startGraph(GraphmlGraph graphmlGraph) throws IOException;

    void makeNode(GraphmlNode node) throws IOException;

    void endNode(Optional<GraphmlLocator> locator) throws IOException;

    void makeEdge(GraphmlHyperEdge edge) throws IOException;

    void endEdge(Optional<GraphmlLocator> graphmlLocator) throws IOException;
}
