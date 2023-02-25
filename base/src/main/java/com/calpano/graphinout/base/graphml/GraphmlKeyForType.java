package com.calpano.graphinout.base.graphml;

import java.util.Arrays;

public enum GraphmlKeyForType {

    Graphml("graphml","all"),  All("all"), Graph("graph"), Node("node"), Edge("edge"), HyperEdge("hyperedge"), Port("port"), Endpoint("endpoint");

    public final String[] values;

    GraphmlKeyForType(String ... values) {
        this.values = values;
    }

    public static GraphmlKeyForType keyForType(String ... keyForType) throws IllegalArgumentException {
        if (keyForType == null || keyForType.length==0)
            // default value
            return GraphmlKeyForType.All;
        for (GraphmlKeyForType v : values()) {
            for(String s : v.values) {
                for(String inputValue:keyForType)
                if(s.equals(inputValue.toLowerCase().trim()))
                    return v;
            }
        }
        throw new IllegalArgumentException("No enum constant  "+ Arrays.toString(keyForType)+".");
    }

}
