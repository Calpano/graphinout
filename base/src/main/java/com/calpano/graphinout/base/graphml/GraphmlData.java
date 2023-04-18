package com.calpano.graphinout.base.graphml;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

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
public class GraphmlData implements XMLValue {

    public static final String TAGNAME = "data";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in data is <b>id</b>
     */
    private String id;

    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private String value;

    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in data is <b>key</b>
     */
    private String key;

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (key != null) attributes.put("key", key);


        return attributes;
    }

}
