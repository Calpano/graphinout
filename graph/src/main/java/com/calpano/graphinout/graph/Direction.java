package com.calpano.graphinout.graph;

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
    private final boolean isDirection;

    private Direction(boolean isDirection) {
        this.isDirection = isDirection;
    }

    public boolean isDirected() {
        return isDirection;
    }

    public static Direction getDirection(String strDirection) {
        switch (strDirection.toLowerCase()) {
            case "in":
                return In;
            case "out":
                return Out;
            case "undirected":
            case "undir":
                return Undirected;
            default:
                return Undirected;
        }
    }
}
