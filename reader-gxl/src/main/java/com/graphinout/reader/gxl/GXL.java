package com.graphinout.reader.gxl;

/**
 * GXL 1.0 element and attribute names (see {@code gxl.dtd}), plus the CJ data keys this module uses to keep GXL
 * details that CJ has no native slot for.
 */
public final class GXL {

    // == elements ==
    public static final String GXL = "gxl";
    public static final String GRAPH = "graph";
    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String REL = "rel";
    public static final String RELEND = "relend";
    public static final String TYPE = "type";
    public static final String ATTR = "attr";

    // == value elements ==
    public static final String BOOL = "bool";
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String STRING = "string";
    public static final String ENUM = "enum";
    public static final String LOCATOR = "locator";
    public static final String SEQ = "seq";
    public static final String SET = "set";
    public static final String BAG = "bag";
    public static final String TUP = "tup";

    // == attributes ==
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ROLE = "role";
    public static final String EDGEIDS = "edgeids";
    public static final String HYPERGRAPH = "hypergraph";
    public static final String EDGEMODE = "edgemode";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String FROMORDER = "fromorder";
    public static final String TOORDER = "toorder";
    public static final String ISDIRECTED = "isdirected";
    public static final String TARGET = "target";
    public static final String DIRECTION = "direction";
    public static final String STARTORDER = "startorder";
    public static final String ENDORDER = "endorder";
    /** {@code xlink:href} on {@code <type>} and {@code <locator>} */
    public static final String HREF = "href";
    public static final String XLINK_HREF = "xlink:href";
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    // == edgemode values ==
    public static final String DIRECTED = "directed";
    public static final String UNDIRECTED = "undirected";
    public static final String DEFAULTDIRECTED = "defaultdirected";
    public static final String DEFAULTUNDIRECTED = "defaultundirected";

    // == relend direction values ==
    public static final String DIR_IN = "in";
    public static final String DIR_OUT = "out";
    public static final String DIR_NONE = "none";

    /** The GXL attr name read as / written from the CJ label. */
    public static final String LABEL_ATTR = "label";

    // == CJ data keys for GXL details without a CJ equivalent ==
    /** graph: {@code <type xlink:href>} (CJ graphs have no type) */
    public static final String KEY_TYPE = "gxl.type";
    /** graph: {@code role} attribute */
    public static final String KEY_ROLE = "gxl.role";
    /** graph: {@code edgemode} attribute, if given explicitly */
    public static final String KEY_EDGEMODE = "gxl.edgemode";
    /** edge: {@code true} if the edge came from a {@code <rel>} (keeps binary rels as rels) */
    public static final String KEY_REL = "gxl.rel";
    /** endpoint of an {@code <edge>}: {@code fromorder}/{@code toorder} */
    public static final String KEY_ORDER = "gxl.order";
    /** endpoint of a {@code <rel>}: {@code startorder} */
    public static final String KEY_STARTORDER = "gxl.startorder";
    /** endpoint of a {@code <rel>}: {@code endorder} */
    public static final String KEY_ENDORDER = "gxl.endorder";
    /**
     * Any element: object mapping attr name to its GXL value kind, for kinds JSON cannot tell apart ({@code enum},
     * {@code locator}, {@code set}, {@code bag}, {@code tup}). bool, int, float, string and seq map to distinct JSON
     * types and need no entry.
     */
    public static final String KEY_KINDS = "gxl.kinds";
    /** Prefix of all keys above; such keys are never written as GXL attrs. */
    public static final String KEY_PREFIX = "gxl.";

    private GXL() {}

}
