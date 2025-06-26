package com.calpano.graphinout.base.cj;

import java.util.HashMap;
import java.util.Map;

public class CjProperties {

    /** Object parts */
    public enum Part {
        Document, Graph, Node, Port, Edge, Endpoint, JSON
    }

    public record Alias(String alias, String resolved, Part... usages) {}

    public static final String NODES = "nodes";
    public static final String GRAPHS = "graphs";
    public static final String EDGES = "edges";
    public static final String PORT = "port";
    public static final String PORTS = "ports";
    public static final String ENDPOINTS = "endpoints";
    public static final String ID = "id";
    public static final String LABEL = "label";
    public static final String DIRECTED = "directed";
    public static final String DIRECTION = "direction";
    public static final String TYPE = "type";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String SOURCE_PORT = "sourcePort";
    public static final String TARGET_PORT = "targetPort";
    public static final String EDGE_DEFAULT = "edgedefault";
    public static final String DATA = "data";
    public static final String KEY = "key";
    public static final String LANGUAGE = "language";
    public static final String VALUE = "value";

    static Map<String, Alias> aliases;

    static {
        aliases = new HashMap<String, Alias>();
        alias(aliases, "dir", CjProperties.DIRECTION, Part.Endpoint);
        alias(aliases, "endpoint", CjProperties.ENDPOINTS, Part.Edge);
        alias(aliases, "edge", CjProperties.EDGES, Part.Graph);
        alias(aliases, "from", CjProperties.SOURCE, Part.Edge);
        alias(aliases, "graph", CjProperties.GRAPHS, Part.Document);
        alias(aliases, "hyperedges", CjProperties.EDGES, Part.Graph);
        //  alias("incoming", CjProperties.SOURCE, Part.Edge);
        alias(aliases, "in", CjProperties.SOURCE, Part.Edge);
        alias(aliases, "name", CjProperties.LABEL, Part.Node);
        alias(aliases, "node", CjProperties.NODES, Part.Graph);
        //  alias("outgoing", CjProperties.TARGET, Part.Edge);
        alias(aliases, "out", CjProperties.TARGET, Part.Edge);
        alias(aliases, "relation", CjProperties.TYPE, Part.Edge);
        alias(aliases, "sources", CjProperties.SOURCE, Part.Edge);
        alias(aliases, "targets", CjProperties.TARGET, Part.Edge);
        alias(aliases, "to", CjProperties.TARGET, Part.Edge);
    }

    public static void alias(Map<String, Alias> aliases, String alias, String resolved, Part... usages) {
        aliases.put(alias, new Alias(alias, resolved, usages));
    }

    public static String normalizeProperty(String property) {
        if (aliases.containsKey(property)) {
            return aliases.get(property).resolved();
        }
        return property;
    }

}
