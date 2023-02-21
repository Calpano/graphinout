package com.calpano.graphinout.base.gio;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ValidatingGioWriter implements GioWriter {


    private final GioWriterImpl gioWriter;
    private final Set<String> nodesIds = new HashSet<>();
    private final Set<String> edgesIds = new HashSet<>();
    private final Set<String> keysIds = new HashSet<>();

    @Override
    public void endDocument() throws IOException {
        gioWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        gioWriter.endEdge();
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        gioWriter.endGraph(locator);
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        gioWriter.endNode(locator);
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        if (keysIds.contains(gioKey.getId())) {
            throw new IllegalStateException("Key ID must be unique: " + gioKey.getId());
        }
        keysIds.add(gioKey.getId());
        gioWriter.key(gioKey);
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        gioWriter.startDocument(document);
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        if (!edge.isValid()) {
            throw new IllegalStateException("GioEdge is not valid: " + edge);
        }
        if (edgesIds.contains(edge.getId())) {
            throw new IllegalStateException("Edge ID must be unique " + edge.getId());
        }
        edgesIds.add(edge.getId());
        gioWriter.startEdge(edge);

    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        //TODO get the nodeID from gioGraph, validate if the set contains, throw error and add if not

        //TODO get hyperEdgeID from gioGraph, validate if the set contains, throw error and add if not

        gioWriter.startGraph(gioGraph);

    }

    @Override
    public void startNode(GioNode node) throws IOException {
        if (nodesIds.contains(node.getId())) {
            throw new IllegalStateException("Node ID must be unique: " + node.getId());
        }
        nodesIds.add(node.getId());
        if (node.getPorts() != null) {
            for (GioPort port : node.getPorts()) {
                if (port.getName() == null || port.getName().isEmpty()) {
                    throw new IllegalStateException("Port name cannot be null or empty.");
                }
            }
        }
        gioWriter.startNode(node);
    }
}
