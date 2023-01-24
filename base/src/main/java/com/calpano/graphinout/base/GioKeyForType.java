package com.calpano.graphinout.base;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.exception.GioExceptionMessage;

public enum GioKeyForType {
    All, Graph, Node, Edge, HyperEdge, Port, Endpoint;

    public static GioKeyForType keyForType(String keyForType) throws GioException {
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
                throw new GioException(GioExceptionMessage.temporary_exemption);
        }
    }
}
