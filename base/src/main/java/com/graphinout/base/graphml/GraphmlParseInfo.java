package com.graphinout.base.graphml;

import java.util.function.BiConsumer;

/**
 * This document defines the parseinfo extension of the GraphML language.
 * <p>
 * It redefines the attribut list of <graph> by adding 7 new attributes:
 *
 * <li>parse.nodeids (fixed to 'canonical' meaning that the id attribute of {@code <node>} follows the
 * pattern 'n[number]),</li>
 * <li>parse.edgeids (fixed to 'canonical' meaning that the id attribute of {@code <edge>} follows the
 * pattern 'e[number]),</li>
 * <li>parse.order (required; one of the values 'nodesfirst', 'adjacencylist' or 'free'),</li>
 * <li>parse.nodes (required; number of nodes in this graph),</li>
 * <li>parse.edges (required; number of edges in this graph),</li>
 *
 * <li>parse.maxindegree (optional; maximal indegree of a node in this graph),</li>
 * <li>parse.maxoutdegree (optional; maximal</li>
 * outdegree of a node in this graph).
 * <p>
 * it redefines the attribute list of node by adding 2 new attributes:
 * <li>parse.indegree (optional; indegree of this node),</li>
 * <li>parse.outdegree (optional; outdegree of this node).</li>
 */
public class GraphmlParseInfo {

    /**
     * Information about the order in which <node> and <edge> elements occur in the <graph>.
     */
    public enum ParseOrder {
        /** all <node> elements appear before the first occurrence of an <edge>. */
        nodesfirst,
        /**
         * the declaration of a <node> is followed by the declaration of its adjacent <edge>s.
         */
        adjacencylist,
        /**
         * no order is imposed.
         */
        free
    }

    public enum Ids {canonical, free}

    public static final ParseOrder DEFAULT_ORDER = ParseOrder.free;

    public static final Ids DEFAULT_IDS = Ids.canonical;

    final Ids nodeIds;
    final Ids edgeIds;
    final ParseOrder order;

    /**
     * The value of the attribute parse.nodes gives the number of <node>s in the <graph>.
     */
    final int nodesCount;
    final int edgesCount;
    Integer maxInDegree;
    Integer maxOutDegree;

    public GraphmlParseInfo(Ids nodeIds, Ids edgeIds, ParseOrder order, int nodesCount, int edgesCount) {
        this.nodeIds = nodeIds;
        this.edgeIds = edgeIds;
        this.order = order;
        this.nodesCount = nodesCount;
        this.edgesCount = edgesCount;
    }

    public void toXmlAttributes(BiConsumer<String, String> attName_attValue) {
        attName_attValue.accept("parse.nodeids", nodeIds.name());
        attName_attValue.accept("parse.edgeids", edgeIds.name());
        attName_attValue.accept("parse.order", order.name());
        attName_attValue.accept("parse.nodes", String.valueOf(nodesCount));
        attName_attValue.accept("parse.edges", String.valueOf(edgesCount));
        if (maxInDegree != null) {
            attName_attValue.accept("parse.maxindegree", String.valueOf(maxInDegree));
        }
        if (maxOutDegree != null) {
            attName_attValue.accept("parse.maxoutdegree", String.valueOf(maxOutDegree));
        }
    }

}
