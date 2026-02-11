package com.graphinout.base.cj;

import com.graphinout.base.cj.document.ICjDocumentMeta;
import com.graphinout.base.cj.document.impl.CjDocumentMetaElement;

public class CjConstants {

    public static final ICjDocumentMeta DEFAULT_META = defaultMeta();

    private static ICjDocumentMeta defaultMeta() {
        CjDocumentMetaElement meta = new CjDocumentMetaElement();
        meta.versionNumber(ConnectedJson.CJ_LATEST_VERSION_NUMBER);
        meta.versionDate(ConnectedJson.CJ_LATEST_VERSION_DATE);
        return meta;
    }

    /** Document-level namespace map for URI expansion (JSON-LD compatible) */
    public static final String CONTEXT = "@context";
    /** Default namespace URI within @context */
    public static final String VOCAB = "@vocab";

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
    public static final String SCHEMA_RESOURCE = "/schema/cj/cj-schema.json";

    private CjConstants() {
        // hidden constructor
    }

}
