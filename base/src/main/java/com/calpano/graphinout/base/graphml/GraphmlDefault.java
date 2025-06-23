package com.calpano.graphinout.base.graphml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

/**
 * @author rbaba

 * @implNote In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to the
 * whole collection of graphs described by the content of <graphml>.
 * <p>
 * These functions are declared by <key> elements (children of <graphml>) and defined by <data> elements.
 * The (optional) <default> child of <key> gives the default value for the corresponding function. Occurence: <key>.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GraphmlDefault implements XMLValue {
    public static final String TAGNAME = "default";
    /**
     * the default value for the corresponding function.
     */
    private String value;
    /**
     * Complex type for the <default> element. default.type is mixed, that is, data may contain #PCDATA. Content type:
     * extension of data-extension.
     * type which is empty per default.
     * <p>
     * The name of this attribute in default is <b>default.type</b>
     */
    private String defaultType;

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (defaultType != null) attributes.put("default.type", defaultType);

        return attributes;
    }

}
