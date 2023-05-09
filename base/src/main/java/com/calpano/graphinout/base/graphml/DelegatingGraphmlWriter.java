package com.calpano.graphinout.base.graphml;

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
    public void data(GraphmlData data) throws IOException {
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
    public void endGraph(@Nullable GraphmlLocator graphmlLocator) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endGraph(graphmlLocator);
        }
    }

    @Override
    public void endHyperEdge() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.endHyperEdge();
        }
    }

    @Override
    public void endNode(@Nullable GraphmlLocator locator) throws IOException {
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
    public void key(GraphmlKey data) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.key(data);
        }
    }

    @Override
    public void startDocument(GraphmlDocument document) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startDocument(document);
        }
    }

    @Override
    public void startEdge(GraphmlEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startEdge(edge);
        }
    }

    @Override
    public void startGraph(GraphmlGraph graphmlGraph) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startGraph(graphmlGraph);
        }
    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startHyperEdge(edge);
        }
    }

    @Override
    public void startNode(GraphmlNode node) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startNode(node);
        }
    }

    @Override
    public void startPort(GraphmlPort port) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.startPort(port);
        }
    }
}
