package com.calpano.graphinout.base.gio;

public enum GioKeyForType {

    Graphml("graphml"),  All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String value;

    GioKeyForType(String value) {
        this.value = value;
    }

    public static GioKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GioKeyForType.All;
        for (GioKeyForType v : values()) {
            if (v.value.equals(keyForType)) return v;
        }
        throw new IllegalArgumentException();
    }
}
