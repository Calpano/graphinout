package com.calpano.graphinout.base;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rbaba
 * @version 0.0.1
 * Provides human-readable descriptions for the GraphML element containing this <desc> as its first child.
 * Occurence: <key>, <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioDescription implements XMLValue {
    private String value;

    @Override
    public String startTag() {
        if (value == null || value.isEmpty())
            return GIOUtil.makeElement("desc");
        return GIOUtil.makeStartElement("desc");
    }

    @Override
    public String valueTag() {
        if (value == null || value.isEmpty()) return "";
        return value + GioGraphInOutConstants.NEW_LINE_SEPARATOR;
    }

    @Override
    public String endTag() {
        if (value == null || value.isEmpty()) return "";
        return GIOUtil.makeEndElement("desc");
    }
}
