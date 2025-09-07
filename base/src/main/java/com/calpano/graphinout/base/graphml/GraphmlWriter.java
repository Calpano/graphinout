package com.calpano.graphinout.base.graphml;

import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 * <p>
 * DESC is considered small and attached to data object. Also DESC is always FIRST in streaming order. DEFAULT value in
 * KEY same.
 */
public interface GraphmlWriter {

    /**
     * Writes a {@code <data>} element.
     * <p>
     * The {@code <data>} element is used to attach application-specific data to GraphML elements.
     * The data is associated with a key, which is defined by a {@code <key>} element.
     * <p>
     * DTD: {@code <!ELEMENT data (#PCDATA)>}
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code key}: The ID of the key for which this data is defined. (Required)</li>
     *     <li>{@code id}: An optional identifier for the data element.</li>
     * </ul>
     *
     * @param data The data to write, including its key and content.
     * @throws IOException if an I/O error occurs.
     */
    void data(IGraphmlData data) throws IOException;

    /**
     * Ends the GraphML document, closing the root {@code <graphml>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void documentEnd() throws IOException;

    /**
     * Starts the GraphML document with the root {@code <graphml>} element.
     * <p>
     * The {@code <graphml>} element is the root element of a GraphML document. It can contain
     * an optional description ({@code <desc>}), any number of {@code <key>} elements for defining custom attributes,
     * and then any number of {@code <data>} or {@code <graph>} elements.
     * <p>
     * DTD: {@code <!ELEMENT graphml (desc?,key*,(data|graph)*)>}
     *
     * @param document The document object, which may contain a description and a list of keys.
     *                 The keys will be written before any graph data.
     * @throws IOException if an I/O error occurs.
     */
    void documentStart(IGraphmlDocument document) throws IOException;

    /**
     * Ends an {@code <edge>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void edgeEnd() throws IOException;

    /**
     * Starts an {@code <edge>} element.
     * <p>
     * The {@code <edge>} element represents an edge in the graph. It can contain an optional description ({@code <desc>}),
     * any number of {@code <data>} elements, and an optional nested {@code <graph>}.
     * <p>
     * DTD: {@code <!ELEMENT edge (desc?,data*,graph?)>}
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code id}: An optional identifier for the edge.</li>
     *     <li>{@code source}: The ID of the source node. (Required)</li>
     *     <li>{@code sourceport}: The name of the port on the source node. (Optional)</li>
     *     <li>{@code target}: The ID of the target node. (Required)</li>
     *     <li>{@code targetport}: The name of the port on the target node. (Optional)</li>
     *     <li>{@code directed}: Whether the edge is directed, overriding the graph's default. (Optional)</li>
     * </ul>
     *
     * @param edge The edge to write, containing its source, target, and other attributes.
     * @throws IOException if an I/O error occurs.
     */
    void edgeStart(IGraphmlEdge edge) throws IOException;

    /**
     * Ends a {@code <graph>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void graphEnd() throws IOException;

    /**
     * Starts a {@code <graph>} element.
     * <p>
     * The {@code <graph>} element is the main container for a graph in GraphML.
     * It can contain an optional description ({@code <desc>}), and either a list of graph elements
     * ({@code <data>}, {@code <node>}, {@code <edge>}, {@code <hyperedge>}) or a {@code <locator>} element
     * that points to the graph data externally.
     * <p>
     * DTD: {@code <!ELEMENT graph (desc?,(((data|node|edge|hyperedge)*)|locator))> }
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code id}: An optional identifier for the graph.</li>
     *     <li>{@code edgedefault}: A required attribute specifying whether edges are directed or undirected by default.</li>
     * </ul>
     *
     * @param graphmlGraph The graph to write, containing its description, ID, and default edge direction.
     *                     It may also contain a locator.
     * @throws IOException if an I/O error occurs.
     */
    void graphStart(IGraphmlGraph graphmlGraph) throws IOException;

    /**
     * Ends a {@code <hyperedge>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void hyperEdgeEnd() throws IOException;

    /**
     * Starts a {@code <hyperedge>} element.
     * <p>
     * A {@code <hyperedge>} connects an arbitrary number of nodes. It can contain an optional description ({@code <desc>}),
     * any number of {@code <data>} or {@code <endpoint>} elements, and an optional nested {@code <graph>}.
     * Endpoints specify the nodes participating in the hyperedge.
     * <p>
     * DTD: {@code <!ELEMENT hyperedge (desc?,(data|endpoint)*,graph?)>}
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code id}: An optional identifier for the hyperedge.</li>
     * </ul>
     *
     * @param edge The hyperedge to write, containing its endpoints and other attributes.
     * @throws IOException if an I/O error occurs.
     */
    void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException;

    /**
     * Writes a {@code <key>} element.
     * <p>
     * The {@code <key>} element declares a custom attribute that can be attached to graph elements.
     * It can have an optional description ({@code <desc>}) and a default value ({@code <default>}).
     * <p>
     * DTD: {@code <!ELEMENT key (desc?,default?)>}
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code id}: The identifier for this key. (Required)</li>
     *     <li>{@code for}: The type of element this key is for (e.g., 'node', 'edge', 'all'). Defaults to 'all'.</li>
     * </ul>
     *
     * @param key The key definition to write, including its ID, target elements, and optional default value.
     * @throws IOException if an I/O error occurs.
     */
    void key(IGraphmlKey key) throws IOException;

    /**
     * Ends a {@code <node>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void nodeEnd() throws IOException;

    /**
     * Starts a {@code <node>} element.
     * <p>
     * The {@code <node>} element represents a node in the graph. It can contain an optional description ({@code <desc>}),
     * and either a combination of {@code <data>} and {@code <port>} elements followed by an optional nested {@code <graph>},
     * or a {@code <locator>} element.
     * <p>
     * DTD: {@code <!ELEMENT node (desc?,((((data|port)*,graph?))|locator))> }
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code id}: A required identifier for the node.</li>
     * </ul>
     *
     * @param node The node to write, containing its ID and other attributes.
     * @throws IOException if an I/O error occurs.
     */
    void nodeStart(IGraphmlNode node) throws IOException;

    /**
     * Ends a {@code <port>} element.
     *
     * @throws IOException if an I/O error occurs.
     */
    void portEnd() throws IOException;

    /**
     * Starts a {@code <port>} element.
     * <p>
     * A {@code <port>} provides a location on a node for edges to connect to. Ports can be nested.
     * It can contain an optional description ({@code <desc>}), and any number of {@code <data>} or nested {@code <port>} elements.
     * <p>
     * DTD: {@code <!ELEMENT port (desc?,(data|port)*)>}
     * <p>
     * Attributes:
     * <ul>
     *     <li>{@code name}: A required name for the port.</li>
     * </ul>
     *
     * @param port The port to write, containing its name and other attributes.
     * @throws IOException if an I/O error occurs.
     */
    void portStart(IGraphmlPort port) throws IOException;

}
