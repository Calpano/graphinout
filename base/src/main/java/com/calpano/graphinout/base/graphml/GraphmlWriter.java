package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlNode;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GraphmlWriter {

    void data(IGraphmlData data) throws IOException;

    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(@Nullable IGraphmlLocator IGraphmlLocator) throws IOException;

    void endHyperEdge() throws IOException;

    void endNode(@Nullable IGraphmlLocator locator) throws IOException;

    void endPort() throws IOException;

    void key(IGraphmlKey data) throws IOException;

    void startDocument(IGraphmlDocument document) throws IOException;

    void startEdge(IGraphmlEdge edge) throws IOException;

    void startGraph(IGraphmlGraph graphmlGraph) throws IOException;

    void startHyperEdge(IGraphmlHyperEdge edge) throws IOException;

    void startNode(IGraphmlNode node) throws IOException;

    void startPort(IGraphmlPort port) throws IOException;


}
