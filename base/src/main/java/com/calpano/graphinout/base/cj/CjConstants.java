package com.calpano.graphinout.base.cj;

public class CjConstants {

    /** Graph base URI for RDF interpretation */
    public static final String ROOT__BASE_URI = "baseUri";

    /** Graph */
    public static final String CANONICAL = "canonical";

    /** Data */
    public static final String DATA = "data";
    /** Edge endpoint direction (in/out/undir) */
    public static final String ENDPOINT__DIRECTION = "direction";
    /** Graph edges */
    public static final String GRAPH__EDGES = "edges";
    /** Edge endpoints */
    public static final String EDGE__ENDPOINTS = "endpoints";
    /** Node nested graphs, Edge nested graphs */
    public static final String GRAPHS = "graphs";
    /** Node id, Edge id, Graph id, Port id */
    public static final String ID = "id";
    /** Node, Edge, Graph, Port */
    public static final String LABEL = "label";
    /** Label */
    public static final String LANGUAGE = "language";
    /** Graph */
    public static final String GRAPH__META = "meta";
    /** Edge Endpoint referenced node id */
    public static final String ENDPOINT__NODE = "node";
    /** Graph nodes */
    public static final String GRAPH__NODES = "nodes";
    /** Edge Endpoint referenced port id */
    public static final String ENDPOINT__PORT = "port";
    /** Node ports */
    public static final String PORTS = "ports";
    /** Edge, Edge Endpoint */
    public static final String EDGE_OR_ENDPOINT__TYPE = "type";
    /** Edge, Edge Endpoint */
    public static final String EDGE_OR_ENDPOINT__TYPE_NODE = "typeNode";
    /** Edge, Edge Endpoint */
    public static final String EDGE_OR_ENDPOINT__TYPE_URI = "typeUri";
    /** Label */
    public static final String VALUE = "value";

    public static final String VERSION_DATE = "versionDate";
    public static final String VERSION_NUMBER = "versionNumber";
    public static final String ROOT__CONNECTED_JSON = "connectedJson";
    public static final String CJ_SCHEMA_LOCATION = "https://calpano.github.io/connected-json/_attachments/cj-schema.json";
    public static final String CJ_SCHEMA_ID = "https://j-s-o-n.org/schema/connected-json/5.0.0";
    public static final String META__CANONICAL = "canonical";
    public static final String META__NODE_COUNT_TOTAL = "nodeCountTotal";
    public static final String META__EDGE_COUNT_TOTAL = "edgeCountTotal";
    public static final String META__NODE_COUNT_IN_GRAPH = "nodeCountInGraph";
    public static final String META__EDGE_COUNT_IN_GRAPH = "edgeCountInGraph";

    private CjConstants() {
        // hidden constructor
    }

}
