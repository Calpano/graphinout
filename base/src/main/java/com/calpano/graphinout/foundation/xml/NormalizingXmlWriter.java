package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static com.calpano.graphinout.foundation.StringFormatter.wrapped;

/**
 * {@link XmlWriter}that normalizes XML by sorting attributes and stripping comments
 */
public class NormalizingXmlWriter extends DelegatingXmlWriter implements XmlWriter {

    public static final String GRAPHML = "graphml";
    static final Map<String, String> graphmlDefaultAttributes = Map.of("xmlns", "http://graphml.graphdrawing.org/xmlns", //
            "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", //
            "xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"
    );
    private static final int MAX_LINE_LENGTH = 60;

    public NormalizingXmlWriter(XmlWriter xmlWriter) {
        super(xmlWriter);
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        if (isInCdata) {
            // Don't normalize CDATA content - preserve it exactly
            super.raw(wrapped(characterData, MAX_LINE_LENGTH));
        } else {
            // Only normalize regular character data
            String norm = characterData.replaceAll("[\n\r\t]", "");
            norm = norm.trim();
            if (!norm.isEmpty()) {
                super.characterData(wrapped(norm, MAX_LINE_LENGTH), false);
            }
        }
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        super.lineBreak();
        // Use TreeMap to sort attributes alphabetically
        Map<String, String> attMap = new TreeMap<>();
        if (name.equals(GRAPHML)) {
            attMap.putAll(graphmlDefaultAttributes);
        }
        attMap.putAll(attributes);
        super.startElement(name, attMap);
    }

}
