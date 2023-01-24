package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.exception.GraphmlException;
import com.calpano.graphinout.base.exception.GraphmlExceptionMessage;

public enum GraphmlKeyForType {
    All, Graph, Node, Edge, HyperEdge, Port, Endpoint;

    public static GraphmlKeyForType keyForType(String keyForType) throws GraphmlException {
        switch (keyForType.toLowerCase()) {
            case "all":
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
                throw new GraphmlException(GraphmlExceptionMessage.temporary_exemption);
        }
    }
}
