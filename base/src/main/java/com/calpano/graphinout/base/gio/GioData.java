package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to
 * the whole collection of graphs described by the content of <graphml>. These functions are declared by <key> elements
 * (children of <graphml>) and defined by <data> elements.
 * Occurence: <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 *
 * @see GioKey {@link GioKey}
 *
 * @author rbaba
 * @version 0.0.1
 * @implNote <p>
 * Structured content can be added within the data element.
 * If you want to add structured content to graph elements you should use the key/data extension mechanism of GraphML.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GioData {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in data is <b>id</b>
     */
    private Optional<String> id;

    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private String value;

    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in data is <b>key</b>.
     *
     * TODO validate: Must refer to a previously defined {@link GioKey#getId()}.
     */
    private String key;
}
