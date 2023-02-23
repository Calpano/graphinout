package com.calpano.graphinout.base.graphml;

import java.io.IOException;

public enum GraphmlKeyForType {
    All, Graph, Node, Edge, HyperEdge, Port, Endpoint;

    public static GraphmlKeyForType keyForType(String keyForType) throws IOException {
        switch (keyForType.toLowerCase()) {
            case "all":
            case "graphml":
                return All;
            case "graph":
                return Graph;
            case "node":
                return Node;
            case "edge":
                return Edge;
            case "hyperedge":
                return HyperEdge;
            case "port":
                return Port;
            case "endpoint":
                return Endpoint;
            default:
                throw new IOException("No enum constant  "+ keyForType+" .");
        }
    }
}
