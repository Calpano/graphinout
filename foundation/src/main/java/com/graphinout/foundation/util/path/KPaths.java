package com.graphinout.foundation.util.path;

import java.util.List;

/**
 * A utility class for creating path representations from strings.
 */
public class KPaths {

    /**
     * Splits a slash-delimited path string into a list of path segments.
     * For more syntax explanations see {@link Step}.
     *
     * @param pathString the string to split, e.g., "a/b/c".
     * @return a {@link List} of path segments.
     */
    public static List<String> of(String pathString) {
        return List.of(pathString.split("/"));
    }

}
