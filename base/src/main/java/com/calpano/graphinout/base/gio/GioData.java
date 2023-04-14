package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to
 * the whole collection of graphs described by the content of <graphml>. These functions are declared by <key> elements
 * (children of <graphml>) and defined by <data> elements.
 * Occurence: <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 *
 * @author rbaba
 * @version 0.0.1
 * @implNote <p>
 * Structured content can be added within the data element.
 * If you want to add structured content to graph elements you should use the key/data extension mechanism of GraphML.
 * @see GioKey {@link GioKey}
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GioData extends GioElement {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in data is <b>id</b>
     */
    private @Nullable String id;
    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private String value;
    /**
     * Links this data instance to a definition given in a {@link GioKey} element
     * <p>
     * TODO validate: Must refer to a previously defined {@link GioKey#getId()}. -- similar to #90
     */
    private String key;

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }
}
