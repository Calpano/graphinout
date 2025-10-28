package com.graphinout.reader.graphml.elements;


public enum GraphmlKeyForType {

    All("all"), //
    Graphml("graphml"), //
    Graph("graph"), //
    Node("node"), //
    Port("port"), //
    Edge("edge"), //
    HyperEdge("hyperedge"), //
    Endpoint("endpoint");

    public final String value;

    GraphmlKeyForType(String value) {
        this.value = value;
    }

    public static GraphmlKeyForType fromGraphmlName(String graphmlName) {
        for (GraphmlKeyForType k : values()) {
            if (k.graphmlName().equals(graphmlName)) return k;
        }
        throw new IllegalArgumentException("No enum constant '" + graphmlName + "'.");
    }

    public static GraphmlKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GraphmlKeyForType.All;
        for (GraphmlKeyForType v : values()) {
            // design decision: we don't warn on wrong casing of attribute values
            if (keyForType.toLowerCase().trim().equals(v.value)) return v;
        }
        throw new IllegalArgumentException("No enum constant '" + keyForType + "'.");
    }

    public String graphmlName() {
        return value;
    }

}
