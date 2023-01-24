package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.xml.XmlWriter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

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
public class GraphmlDescription {
    private String value;

    public void writeXml(XmlWriter xmlWriter) throws IOException {
        if(value==null)
            return;
        xmlWriter.startElement("desc");
        xmlWriter.charaterData(value);
        xmlWriter.endElement("desc");
    }

}
