package com.graphinout.base.cj;

public class CjConstants {

    /** Graph base URI for RDF interpretation */
    public static final String BASE_URI = "baseUri";

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
    public static final String LABEL_ENTRIES = "entries";
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
    /** Node */
    public static final String NODE_TYPES = "types";
    /** Label */
    public static final String VALUE = "value";

    public static final String CONNECTED_JSON__VERSION_DATE = "versionDate";
    public static final String CONNECTED_JSON__VERSION_NUMBER = "versionNumber";
    public static final String CONNECTED_JSON__CANONICAL = "canonical";
    public static final String ROOT__CONNECTED_JSON = "connectedJson";
    public static final String CJ_SCHEMA_URL = "https://j-s-o-n.org/schema/cj-7.0.0.json";
    public static final String CJ_SCHEMA_ID = "https://j-s-o-n.org/schema/cj-7.0.0.json";
    public static final String SCHEMA_RESOURCE = "/schema/cj/cj-schema.json";
    public static final String SCHEMA_GIT_URL = "https://github.com/Calpano/connected-json/blob/main/modules/ROOT/attachments/cj-schema.json";
    public static final String CJ_LATEST_VERSION_DATE = "2026-01-15";
    public static final String CJ_LATEST_VERSION_NUMBER = "7.0.0";

    private CjConstants() {
        // hidden constructor
    }

}
