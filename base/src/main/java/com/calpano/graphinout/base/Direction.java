package com.calpano.graphinout.base;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote <p>
 * Graphs in GraphML are mixed, in other words, they can contain directed and undirected edges at the same time.
 * If no direction is specified when an edge is declared, the default direction is applied to the edge.
 * The default direction is declared as the XML Attribute edgedefault of the graph element.
 * The two possible value for this XML Attribute are directed and undirected.
 * Note that the default direction must be specified.
 */
public enum Direction {
    In(true), Out(true), Undirected(false);
    private final boolean isDirected;

    Direction(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public static Direction getDirection(String strDirection) {
        return switch (strDirection.toLowerCase()) {
            case "in" -> In;
            case "out" -> Out;
            case "undir" -> Undirected;
            default -> throw new IllegalArgumentException("Could not interpret '" + strDirection +
                    "' as graphml endpoint direction");
        };
    }

    public boolean isDirected() {
        return isDirected;
    }

    public String xmlValue() {
        return switch (this) {
            case In -> "in";
            case Out -> "out";
            case Undirected -> "undir";
        };
    }
}
