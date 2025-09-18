package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Direction for edge endpoints according to Connected JSON specification.
 */
public enum CjDirection {
    IN("in"),
    OUT("out"),
    UNDIR("undir");

    private final String value;

    CjDirection(String value) {
        this.value = value;
    }

    public static CjDirection of(@Nullable String value) {
        for (CjDirection direction : values()) {
            if (direction.value.equals(value)) {
                return direction;
            }
        }
        return UNDIR; // default
    }

    public boolean isDirected() {
        return this == IN || this == OUT;
    }

    public boolean isUndirected() {
        return this == UNDIR;
    }

    public String value() {
        return value;
    }
}
