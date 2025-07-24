package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DelegatingGraphmlWriter implements GraphmlWriter {

    private final List<GraphmlWriter> writers;

    public DelegatingGraphmlWriter(GraphmlWriter... writers) {
        this.writers = Arrays.asList(writers);
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.data(data);
        }
    }

    @Override
    public void endDocument() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endDocument();
        }
    }

    @Override
    public void endEdge() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endEdge();
        }
    }

    @Override
    public void endGraph(@Nullable IGraphmlLocator IGraphmlLocator) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endGraph(IGraphmlLocator);
        }
    }

    @Override
    public void endHyperEdge() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endHyperEdge();
        }
    }

    @Override
    public void endNode(@Nullable IGraphmlLocator locator) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endNode(locator);
        }
    }

    @Override
    public void endPort() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endPort();
        }
    }

    @Override
    public void key(IGraphmlKey data) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.key(data);
        }
    }

    @Override
    public void startDocument(IGraphmlDocument document) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startDocument(document);
        }
    }

    @Override
    public void startEdge(IGraphmlEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startEdge(edge);
        }
    }

    @Override
    public void startGraph(IGraphmlGraph graphmlGraph) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startGraph(graphmlGraph);
        }
    }

    @Override
    public void startHyperEdge(IGraphmlHyperEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startHyperEdge(edge);
        }
    }

    @Override
    public void startNode(IGraphmlNode node) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startNode(node);
        }
    }

    @Override
    public void startPort(IGraphmlPort port) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startPort(port);
        }
    }

}
