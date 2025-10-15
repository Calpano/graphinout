package com.graphinout.base.gio;

public enum GioKeyForType {

    Graphml("graphml"),
    /** the default value */
    All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String value;

    GioKeyForType(String value) {
        this.value = value;
    }

    /**
     * Compare ignoring case
     *
     * @param keyForType
     * @return
     * @throws IllegalArgumentException
     */
    public static GioKeyForType keyForType(String keyForType) throws IllegalArgumentException {
        if (keyForType == null)
            // default value
            return GioKeyForType.All;
        for (GioKeyForType v : values()) {
            if (keyForType.toLowerCase().trim().equals(v.value)) return v;
        }
        throw new IllegalArgumentException("No enum constant  " + keyForType + " .");
    }
}
