package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.foundation.xml.XmlFormatter;

public class GraphmlFormatter {

    public static String normalize(String xmlIn) {
        return XmlFormatter.normalize(xmlIn, GraphmlElements.setOfContentElementNames(), NormalizingGraphmlWriter::new);
    }

}
