package com.calpano.graphinout.base;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
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
public class GioData implements XMLValue {

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
    public String startTag() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (key != null) attributes.put("key", key);

        if (value == null) return GIOUtil.makeElement("data", attributes);
        else return GIOUtil.makeStartElement("data", attributes);

    }

    @Override
    public String valueTag() {
        if (value == null) return "";
        return value + GioGraphInOutConstants.NEW_LINE_SEPARATOR;
    }

    @Override
    public String endTag() {
        if (value == null || value.isEmpty()) return "";
        else return GIOUtil.makeEndElement("data");
    }
}
