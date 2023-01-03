package com.calpano.graphinout.maxgraph;

public class GioEndpoint {

    /** Direction from edge to node */
    public enum Direction {
        In, Out, Undirected
    }

    Direction direction;
    GioNode node;

}
