package com.calpano.graphinout.foundation.xml;

import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import java.io.File;

public class XmlTool {

    public static void parseAndWriteXml(File xmlFile, XmlWriter xmlWriter) throws Exception {
        SAXParser saxParser = XmlFactory.createSaxParser();
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, null);
        XMLReader reader = saxParser.getXMLReader();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler2XmlWriter);
        reader.setContentHandler(handler2XmlWriter);
        reader.parse(xmlFile.getAbsolutePath());
    }

}
