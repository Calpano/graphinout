package com.calpano.graphinout.converter.standard.xml;

import com.calpano.graphinout.graph.*;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    //    public GioGraphML createGioGraphML() {
//        return new StandardGraphML();
//    }
    public StandardGraphML createStandardGraphML() {
        return new StandardGraphML();
    }

    public GioGraph createGioGraph() {
        return new GioGraph();
    }

    public GioEdge createGioEdge() {
        return new GioEdge();
    }

    public GioNode createGioNode() {
        return new GioNode();
    }

    public GioKey createGioKey() {
        return new GioKey();
    }

    public GioNodeData createGioNodeData() {
        return new GioNodeData();
    }

    public GioEdgeData createGioEdgeData() {
        return new GioEdgeData();
    }

    public GioEndpoint creatreGioEndpoint() {
        return new GioEndpoint();
    }

    public GioHyperEdge creatreGioHyperEdge() {
        return new GioHyperEdge();
    }

    public GioPort creatreGioPort() {
        return new GioPort();
    }

}
