package com.graphinout.foundation.xml;


import com.graphinout.base.reader.Location;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.ContentErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Listen to SAX events and map them to XmlWriter events. No stack is used, as XML validation either happened before or
 * will happen as XmlWRiter impl.
 */
public class Sax2XmlWriter extends DefaultHandler implements LexicalHandler {

    private static final Logger log = LoggerFactory.getLogger(Sax2XmlWriter.class);
    private final XmlWriter xmlWriter;
    private final Map<String, String> namespaces = new HashMap<>();
    private final SaxCharBuffer saxCharBuffer;
    private boolean isFirstElement = true;

    public Sax2XmlWriter(XmlWriter xmlWriter) {
        assert xmlWriter != null;
        this.xmlWriter = xmlWriter;
        this.saxCharBuffer = new SaxCharBuffer(xmlWriter);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        try {
            // no escaping happening here
            saxCharBuffer.characters(s);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            saxCharBuffer.charactersEnd();
            // no comments: we drop them.
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endCDATA() throws SAXException {
        try {
            saxCharBuffer.kindEnd();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDTD() {}

    @Override
    public void endDocument() throws SAXException {
        try {
            saxCharBuffer.charactersEnd();
            xmlWriter.documentEnd();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            saxCharBuffer.charactersEnd();
            xmlWriter.elementEnd(uri, localName, qName);
        } catch (ContentErrorException e) {
            // already handled
        } catch (Exception e) {
            throw xmlWriter.sendContentError_Error("endElement", e);
        }
    }

    @Override
    public void endEntity(String name) {}

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw xmlWriter.sendContentError_Error("SAX error", e);
    }

    @Override
    public void ignorableWhitespace(char[] chars, int start, int length)
            throws SAXException {
        try {
            saxCharBuffer.kindStart(CharactersKind.IgnorableWhitespace);
            saxCharBuffer.characters(new String(chars, start, length));
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void setDocumentLocator(Locator saxLocator) {
        this.xmlWriter.setLocator(new com.graphinout.base.reader.Locator() {
            @Override
            public Location location() {
                return new Location(saxLocator.getLineNumber(), saxLocator.getColumnNumber());
            }
        });
    }

    @Override
    public void startCDATA() throws SAXException {
        try {
            saxCharBuffer.kindStart(CharactersKind.CDATA);
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
            xmlWriter.documentStart();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            // if this element starts, the previous one ended
            // as in 'this <em>very cool</em> planet'
            saxCharBuffer.charactersEnd();
            // SAX parsers have no attribute order defined, so we auto-sort lexicographically
            Map<String, String> attMap = new TreeMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                String attributesQName = attributes.getQName(i);
                String attributesValue = attributes.getValue(i);
                attMap.put(attributesQName, attributesValue);
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
            xmlWriter.elementStart(uri, localName, qName, attMap);
        } catch (ContentErrorException e) {
            // this one is already logged
        } catch (Exception e) {
            throw xmlWriter.sendContentError_Error("While parsing start element <"+qName+"> with atts="+toString(attributes)+": "+e.getMessage(), e);
        }
    }

    private static String toString(Attributes attributes) {
        if(attributes.getLength()==0)
            return "--";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attributes.getLength(); i++) {
            String attributesQName = attributes.getQName(i);
            String attributesValue = attributes.getValue(i);
            sb.append(attributesQName).append("=\"").append(attributesValue).append("\" ");
        }
        return sb.toString();
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
        xmlWriter.onContentError(new ContentError(ContentError.ErrorLevel.Warn, e.getMessage(), new Location(e.getLineNumber(), e.getColumnNumber())));
    }

    private void onContentError(ContentError contentError) {
        xmlWriter.onContentError(contentError);
    }

}
