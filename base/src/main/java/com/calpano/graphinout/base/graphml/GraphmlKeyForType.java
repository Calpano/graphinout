package com.calpano.graphinout.base.graphml;

public enum GraphmlKeyForType {

    Graphml("graphml"), All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String value;

    GraphmlKeyForType(String value) {
        this.value = value;
    }

    public static GraphmlKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GraphmlKeyForType.All;
        for (GraphmlKeyForType v : values()) {
            // design decision: we don't warn on wrong casing of attribute values
            if (keyForType.toLowerCase().trim().equals(v.value))
                return v;
        }
        throw new IllegalArgumentException("No enum constant '" + keyForType + "'.");
    }

}
