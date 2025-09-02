package com.calpano.graphinout.base.graphml;

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
    public void documentEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.documentEnd();
        }
    }

    @Override
    public void documentStart(IGraphmlDocument document) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.documentStart(document);
        }
    }

    @Override
    public void edgeEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.edgeEnd();
        }
    }

    @Override
    public void edgeStart(IGraphmlEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.edgeStart(edge);
        }
    }

    @Override
    public void graphEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.graphEnd();
        }
    }

    @Override
    public void graphStart(IGraphmlGraph graphmlGraph) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.graphStart(graphmlGraph);
        }
    }

    @Override
    public void hyperEdgeEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.hyperEdgeEnd();
        }
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.hyperEdgeStart(edge);
        }
    }

    @Override
    public void key(IGraphmlKey data) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.key(data);
        }
    }

    @Override
    public void nodeEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.nodeEnd();
        }
    }

    @Override
    public void nodeStart(IGraphmlNode node) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.nodeStart(node);
        }
    }

    @Override
    public void portEnd() throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.portEnd();
        }
    }

    @Override
    public void portStart(IGraphmlPort port) throws IOException {
        for (GraphmlWriter writer : writers) {
            writer.portStart(port);
        }
    }

}
