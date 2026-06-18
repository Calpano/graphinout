package com.graphinout.reader.grale;

import java.util.List;

/**
 * Shared constants for the grale format mapping. See {@link GraleReader} for the overall scheme.
 */
final class Grale {

    private Grale() {}

    /** Key on the CJ graph {@code data} holding the preserved envelope-level grale object. */
    static final String WRAPPER_KEY = "grale";

    /** Envelope-level fields (siblings of {@code nodes}/{@code edges}) preserved verbatim. */
    static final List<String> GRAPH_LEVEL_KEYS =
            List.of("options", "value", "hyperedges", "diagnostics", "debug");

    /** Reserved node-{@code data} key carrying a compound node's {@code parent}. */
    static final String PARENT_KEY = "$gioParent";

    /** Reserved edge-{@code data} key carrying a multigraph edge's {@code name}. */
    static final String NAME_KEY = "$gioName";
}
