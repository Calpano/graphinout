package com.calpano.graphinout.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
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
     * The name of this attribute in graph is <b>id</b>
     */
    protected String id;

    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    protected String value;

    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in graph is <b>key</b>
     */
    private String key;
}
