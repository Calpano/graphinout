package com.calpano.graphinout.base.graphml;

public enum GraphmlKeyForType {

    Graphml("graphml","all"),  All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String[] values;

    GraphmlKeyForType(String ... values) {
        this.values = values;
    }

    public static GraphmlKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GraphmlKeyForType.All;
        for (GraphmlKeyForType v : values()) {
            for(String s : v.values) {
                // design decision: we don't warn on wrong casing of attribute values
                if(s.equalsIgnoreCase(keyForType))
                    return v;
            }
        }
        throw new IllegalArgumentException("No enum constant '"+ keyForType+"'.");
    }

}
