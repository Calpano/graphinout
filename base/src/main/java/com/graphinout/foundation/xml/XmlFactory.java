package com.graphinout.foundation.xml;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class XmlFactory {

    private static final Logger log = getLogger(XmlFactory.class);

    public static SAXParser createSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false); // Explicitly disable DTD validation
        // For security, disable external entity loading which also prevents XSD lookups
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // report namespace declarations as attributes
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
        } catch (Exception e) {
            // Some parsers may not support all features
            log.warn("Warning: Could not set a security feature on the SAX parser.");
        }
        return factory.newSAXParser();
    }

}
