package com.calpano.graphinout.graph;

public class GioEndpoint {

    Direction direction;
    GioNode node;
    /**
     * Direction from edge to node
     */
    public enum Direction {
        In, Out, Undirected
    }

}
