/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.graphinout.base.graphml;


import java.util.Set;

/**
 * GraphML XML element names as defined in the spec
 *
 * @author rbaba
 */
public class GraphmlElements {

    public static final String EDGE = "edge";
    public static final String ENDPOINT = "endpoint";
    public static final String GRAPH = "graph";
    public static final String GRAPHML = "graphml";
    public static final String HYPER_EDGE = "hyperedge";
    public static final String KEY = "key";
    public static final String NODE = "node";
    public static final String DATA = "data";
    public static final String PORT = "port";
    public static final String DESC = "desc";
    public static final String LOCATOR = "locator";

    public static final String DEFAULT = "default";
    public static Set<String> SET_OF_HAS_DATA_ELEMENTS = Set.of(EDGE, GRAPH, NODE, PORT, ENDPOINT, HYPER_EDGE);

    /** Elements with significant whitespace */
    public static Set<String> SET_OF_CONTENT_ELEMENTS = Set.of(DATA, DESC, DEFAULT);


}
