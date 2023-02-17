package com.calpano.graphinout.base.gio;

public enum GioEndpointDirecton {

    In, Out, Undirected;

    public boolean isDirected() {
        return this == In || this == Out;
    }

    public boolean isUndirected() {
        return this == Undirected;
    }

}
