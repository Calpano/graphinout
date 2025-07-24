package com.calpano.graphinout.foundation.xml;


import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Listen to SAX events and map them to XmlWriter events. No stack is used, as XML validation either happened before or
 * will happen as XmlWRiter impl.
 */
class Sax2XmlWriter extends DefaultHandler implements LexicalHandler {

    private static final Logger log = LoggerFactory.getLogger(Sax2XmlWriter.class);
    private final XmlWriter xmlWriter;
    private final @Nullable Consumer<ContentError> errorConsumer;
    private final Map<String, String> namespaces = new HashMap<>();
    private Locator locator;
    private boolean isFirstElement = true;

    public Sax2XmlWriter(XmlWriter xmlWriter, @Nullable Consumer<ContentError> errorConsumer) {
        assert xmlWriter != null;
        this.xmlWriter = xmlWriter;
        this.errorConsumer = errorConsumer;
    }

    private static String tagName(String uri, String localName, String qName) {
        String tagName = localName;
        if (uri.isEmpty()) tagName = qName;
        else if (tagName.isEmpty()) {
            tagName = qName;
        }
        if (localName.isEmpty()) {
            tagName = qName;
        }
        return tagName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        // log.trace("characters [{}].", s);
        try {
            xmlWriter.characterData(s, false);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) {
        // no comments
    }

    @Override
    public void endCDATA() throws SAXException {
        try {
            xmlWriter.endCDATA();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDTD() {}

    @Override
    public void endDocument() throws SAXException {
        try {
            xmlWriter.endDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //    log.debug("XML </{}>", qName);
        String tagName = tagName(uri, localName, qName);
        try {
            xmlWriter.endElement(tagName);
        } catch (Exception e) {
            throw buildError(e);
        }
    }

    @Override
    public void endEntity(String name) {}

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw buildError(e);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startCDATA() throws SAXException {
        try {
            xmlWriter.startCDATA();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    // LexicalHandler methods not used
    @Override
    public void startDTD(String name, String publicId, String systemId) {}

    @Override
    public void startDocument() throws SAXException {
        try {
            xmlWriter.startDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // log.debug("XML <{}>.", qName);
        String tagName = tagName(uri, localName, qName);
        try {
            Map<String, String> attMap = new HashMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                attMap.put(attributes.getQName(i), attributes.getValue(i));
            }
            // On the first element, add all namespace declarations as xmlns attributes
            if (isFirstElement) {
                for (Map.Entry<String, String> ns : namespaces.entrySet()) {
                    String prefix = ns.getKey();
                    String nsAttr = prefix.isEmpty() ? "xmlns" : ("xmlns:" + prefix);
                    attMap.put(nsAttr, ns.getValue());
                }
                isFirstElement = false;
            }
            xmlWriter.startElement(tagName, attMap);
        } catch (Exception e) {
            throw buildError(e);
        }
    }

    @Override
    public void startEntity(String name) {}

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        namespaces.put(prefix, uri);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        //noinspection ThrowableNotThrown
        buildException(ContentError.ErrorLevel.Warn, e);
    }

    private RuntimeException buildError(Exception e) {
        return buildException(ContentError.ErrorLevel.Error, e);
    }


    private @Nullable RuntimeException buildException(ContentError.ErrorLevel errorLevel, Exception e) {
        ContentError contentError = new ContentError(errorLevel, e.getMessage(), locator == null ? null : new Location(locator.getLineNumber(), locator.getColumnNumber()));
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
        String location = locator == null ? "N/A" : locator.getLineNumber() + ":" + locator.getColumnNumber();
        if (errorLevel == ContentError.ErrorLevel.Error) {
            return new RuntimeException("While parsing " + location + "\n" + "Message: " + e.getMessage(), e);
        } else {
            log.warn("ContentError: " + contentError, e);
            return null;
        }
    }

}
