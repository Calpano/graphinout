package com.graphinout.base.gio;

import com.graphinout.base.cj.CjDirection;

public enum GioEndpointDirection {

    In("in"), Out("out"), Undirected("undir");

    public final String xmlValue;

    GioEndpointDirection(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public static GioEndpointDirection of(String value) {
        for (GioEndpointDirection d : values()) {
            if (d.xmlValue.equalsIgnoreCase(value))
                return d;
        }
        throw new IllegalArgumentException("Could not interpret '" + value + "' as GioEndpointDirection");
    }

    public boolean isDirected() {
        return this == In || this == Out;
    }

    public boolean isUndirected() {
        return this == Undirected;
    }

    public CjDirection toCjDirection() {
        return switch (this) {
            case In -> CjDirection.IN;
            case Out -> CjDirection.OUT;
            case Undirected -> CjDirection.UNDIR;
        };
    }
}
