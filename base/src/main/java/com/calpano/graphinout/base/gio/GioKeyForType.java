package com.calpano.graphinout.base.gio;

public enum GioKeyForType {

    Graphml("graphml","all"),
    /** the default value */
    All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String[] values;

    GioKeyForType(String ... values) {
        this.values = values;
    }

    public static GioKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GioKeyForType.All;
        for (GioKeyForType v : values()) {
            for(String s : v.values) {
                if(s.equals(keyForType))
                    return v;
            }
        }
        throw new IllegalArgumentException("No enum constant  "+ keyForType+" .");
    }
}
