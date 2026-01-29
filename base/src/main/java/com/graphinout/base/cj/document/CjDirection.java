package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Direction for edge endpoints according to Connected JSON specification.
 */
public enum CjDirection {
    IN("in"), OUT("out"), UNDIR("undir");

    public static final CjDirection DEFAULT = CjDirection.UNDIR;

    private final String value;

    CjDirection(String value) {
        this.value = value;
    }

    /**
     * @param value may be null for default (undirected)
     * @throws IllegalArgumentException if value is invalid (null is ok)
     */
    public static @NonNull CjDirection of(@Nullable String value) throws IllegalArgumentException {
        for (CjDirection direction : values()) {
            if (direction.value.equals(value)) {
                return direction;
            }
        }
        if (value == null) return UNDIR; // default
        throw new IllegalArgumentException("Unknown direction: " + value);
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
