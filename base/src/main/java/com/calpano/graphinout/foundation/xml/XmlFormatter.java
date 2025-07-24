package com.calpano.graphinout.foundation.xml;

import org.slf4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.StringReader;

import static org.slf4j.LoggerFactory.getLogger;

public class XmlFormatter {

    private static final Logger log = getLogger(XmlFormatter.class);

    /**
     * Canonicalize. Element order is strict and kept. Attribute order is sorted alphabetically. Comments are stripped.
     */
    public static String normalize(String xml) {
        try {
            Xml2StringWriter xmlWriter = new Xml2StringWriter();
            NormalizingXmlWriter normalizingXmlWriter = new NormalizingXmlWriter(xmlWriter);
            Sax2XmlWriter sax2xmlWriter = new Sax2XmlWriter(normalizingXmlWriter, error-> log.error("Error in XML: {}", error));

            SAXParser saxParser = XmlFactory.createSaxParser();
            XMLReader reader = saxParser.getXMLReader();
            reader.setContentHandler(sax2xmlWriter);
            reader.setProperty(XmlTool.PROPERTIES_LEXICAL_HANDLER, sax2xmlWriter);
            reader.setErrorHandler(sax2xmlWriter);

            InputSource inputSource = new InputSource(new StringReader(xml));
            reader.parse(inputSource);
            return xmlWriter.string();
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse/normalize XML=----\n" + xml, e);
        } catch (IOException e) {
            throw new RuntimeException("IO in parse XML", e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
