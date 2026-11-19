package com.graphinout.base.xml.factory;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The one place to create SAX parsers for (possibly untrusted) input.
 * <p>
 * All parsers are hardened against XML external entity (XXE) attacks: they never load an external DTD and never
 * resolve external general or parameter entities, so an input can not make us read local files or fetch URLs.
 * Internal entities ({@code <!ENTITY x "text">}) still work; secure processing bounds their expansion.
 */
public class XmlFactory {

    private static final Logger log = getLogger(XmlFactory.class);

    public static SAXParser createSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = hardenedSaxParserFactory();
        try {
            // report namespace declarations as attributes
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (Exception e) {
            log.warn("Could not enable namespace-prefixes on the SAX parser.", e);
        }
        return factory.newSAXParser();
    }

    /**
     * A namespace-aware, non-validating {@link SAXParserFactory} with external entities and external DTDs disabled.
     * Fails rather than returning a factory where these can not be disabled. Use it directly for schema validation
     * ({@link SAXParserFactory#setSchema}) or to build an {@link org.xml.sax.XMLReader} for a {@code SAXSource}.
     */
    public static SAXParserFactory hardenedSaxParserFactory() throws ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false); // no DTD validation
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (SAXException e) {
            throw new ParserConfigurationException("SAX parser does not support disabling external entities: " + e.getMessage());
        }
        return factory;
    }

}
