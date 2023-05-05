package com.calpano.graphinout.base.gio;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ValidatingGioWriter implements GioWriter {

    private final Set<String> nodesIds = new HashSet<>();
    private final Set<String> edgesIds = new HashSet<>();
    private final Set<String> keysIds = new HashSet<>();
    private final Set<String> endpointsNode = new HashSet<>();
    private final Set<String> endpointsPort = new HashSet<>();
    private final Set<String> nodePortName = new HashSet<>();

    @Override
    public void data(GioData data) throws IOException {
        if (!keysIds.contains(data.getKey()))
            throw new IllegalStateException("GioData should refer to an existing Key ID, but uses '" + data.getKey() + "'");
    }

    @Override
    public void endDocument() throws IOException {

    }

    @Override
    public void endEdge() throws IOException {

    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        if (!nodesIds.containsAll(endpointsNode))
            throw new IllegalStateException("All GioEdge endpoints should refer to an existing GioNode ID.");
        if (!nodePortName.isEmpty()) {
            List<String> missingPorts = new ArrayList<>();
            for (String port : endpointsPort) {
                if (!nodePortName.contains(port)) {
                    missingPorts.add(port);
                }
            }
            if (!missingPorts.isEmpty()) {
                String errorMsg = "All GioEdge Endpoint Port should refer to an existing GioNode Port. Missing port(s): " + missingPorts;
                throw new IllegalStateException(errorMsg);
            }
        }

    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {

    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        if (keysIds.contains(gioKey.getId())) {
            throw new IllegalStateException("GioKey ID must be unique: " + gioKey.getId());
        }
        keysIds.add(gioKey.getId());
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {

    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        if (edge.getId() != null) {
            boolean contains = edgesIds.contains(edge.getId());
            if (contains) {
                throw new IllegalStateException("GioEdge ID must be unique: " + edge.getId());
            } else {
                edgesIds.add(edge.getId());
            }
        }

        if (edge.getEndpoints().size() < 2) throw new IllegalStateException("GioEdge must have at least 2 endpoints.");
        for (GioEndpoint endpoint : edge.getEndpoints()) {
            endpointsNode.add(endpoint.getNode());
            endpointsPort.add(endpoint.getPort());
        }
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {


    }

    @Override
    public void startNode(GioNode node) throws IOException {
        if (node.getId() == null || node.getId().isEmpty())
            throw new IllegalStateException("GioNode must have and ID.");
        if (nodesIds.contains(node.getId())) {
            throw new IllegalStateException("GioNode ID must be unique: " + node.getId());
        }
        nodesIds.add(node.getId());

    }

    public void startPort(GioPort port) throws IOException {
        String portName = port.getName();
        nodePortName.add(portName);
        if (portName == null || portName.isEmpty()) {
            throw new IllegalStateException("GioPort name cannot be null or empty.");
        }

    }

    public void endPort() throws IOException {

    }

}
