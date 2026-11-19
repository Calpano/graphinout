package com.graphinout.reader.gxl;

import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses GXL into a small element tree. GXL nests graphs in nodes, edges and rels and nests values in composite
 * values, so the CJ mapping is easier on a tree than on SAX events.
 *
 * <p>The parser never fetches the DTD that GXL files typically reference
 * ({@code <!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">}) and resolves no external entities. It is not
 * namespace-aware, so {@code xlink:href} also works in files that rely on the DTD's fixed {@code xmlns:xlink}
 * declaration, which is never loaded.
 */
final class GxlXml {

    /** One XML element; attributes keep their qualified names (e.g. {@code xlink:href}). */
    static final class Element {
        final String name;
        final Map<String, String> attributes;
        final List<Element> children = new ArrayList<>();
        final StringBuilder text = new StringBuilder();
        final Location location;

        Element(String name, Map<String, String> attributes, Location location) {
            this.name = name;
            this.attributes = attributes;
            this.location = location;
        }

        @Nullable String attr(String name) {
            return attributes.get(name);
        }

        /** {@code xlink:href}, also when bound to another prefix or used without one */
        @Nullable String href() {
            String v = attributes.get(GXL.XLINK_HREF);
            if (v != null) return v;
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                String k = e.getKey();
                if (k.equals(GXL.HREF) || k.endsWith(":" + GXL.HREF)) return e.getValue();
            }
            return null;
        }

        String text() {
            return text.toString();
        }

        List<Element> children(String name) {
            return children.stream().filter(c -> c.name.equals(name)).toList();
        }

        @Nullable Element firstChild(String name) {
            for (Element c : children) {
                if (c.name.equals(name)) return c;
            }
            return null;
        }
    }

    private GxlXml() {}

    static Element parse(InputSource input) throws IOException, SAXException, ParserConfigurationException {
        TreeBuilder builder = new TreeBuilder();
        createParser().parse(input, builder);
        if (builder.root == null) {
            throw new SAXException("Empty XML document");
        }
        return builder.root;
    }

    private static SAXParser createParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newSAXParser();
    }

    private static final class TreeBuilder extends DefaultHandler {

        private final Deque<Element> stack = new ArrayDeque<>();
        private @Nullable Element root;
        private @Nullable Locator locator;

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        /** Belt and braces next to the parser features: never resolve an external entity or DTD. */
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            Map<String, String> attributes = new LinkedHashMap<>();
            for (int i = 0; i < atts.getLength(); i++) {
                attributes.put(atts.getQName(i), atts.getValue(i));
            }
            Location loc = locator == null ? Location.UNAVAILABLE
                    : new Location(locator.getLineNumber(), locator.getColumnNumber());
            Element e = new Element(qName, attributes, loc);
            Element parent = stack.peek();
            if (parent == null) {
                root = e;
            } else {
                parent.children.add(e);
            }
            stack.push(e);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            stack.pop();
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            Element top = stack.peek();
            if (top != null) {
                top.text.append(ch, start, length);
            }
        }
    }

}
