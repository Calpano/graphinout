package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class DelegatingGioWriter implements GioWriter {

    public DelegatingGioWriter(GioWriter ... writers) {
        this.writers = Arrays.asList(writers);
    }

    private final List<GioWriter> writers;


    @Override
    public void endDocument() throws IOException {
        for (GioWriter writer : writers) {
            writer.endDocument();
        }
    }

    @Override
    public void endEdge() throws IOException {
        for (GioWriter writer : writers) {
            writer.endEdge();
        }
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        for (GioWriter writer : writers) {
            writer.endGraph(locator);
        }
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        for (GioWriter writer : writers) {
            writer.endNode(locator);
        }
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        for (GioWriter writer : writers) {
            writer.key(gioKey);
        }
    }

    @Override
    public void data(GioData data) throws IOException {
        for (GioWriter writer : writers) {
            writer.data(data);
        }
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        for (GioWriter writer : writers) {
            writer.startDocument(document);
        }
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        for (GioWriter writer : writers) {
            writer.startEdge(edge);
        }
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        for (GioWriter writer : writers) {
            writer.startGraph(gioGraph);
        }
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        for (GioWriter writer : writers) {
            writer.startNode(node);
        }
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        for (GioWriter writer : writers) {
            writer.startPort(port);
        }
    }

    @Override
    public void endPort() throws IOException {
        for (GioWriter writer : writers) {
            writer.endPort();
        }
    }
}
