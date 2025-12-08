package com.graphinout.foundation.pure.xml;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.value.BooleanRef;
import com.graphinout.foundation.pure.value.ObjectRef;
import com.graphinout.foundation.pure.xml.document.IXmlContent;
import com.graphinout.foundation.pure.xml.document.IXmlNode;
import com.graphinout.foundation.pure.xml.document.XmlDocument;
import com.graphinout.foundation.pure.xml.document.XmlElement;
import com.graphinout.foundation.pure.xml.document.XmlRaw;
import com.graphinout.foundation.pure.xml.document.XmlText;
import com.graphinout.foundation.pure.xml.escape.XmlEscapers;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.value.ObjectRef.objectRef;

public class XmlSerializer {

    public static void contentToXmlString(IXmlContent hasContent, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        for (IXmlNode node : Java9.Stream.toList(hasContent.directChildren())) {
            nodeToXmlString(node, a, markAsNonPlainString, xmlSpace);
        }
    }

    /**
     * @param xmlSpace application setting
     */
    public static void documentToXmlString(XmlDocument document, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        markAsNonPlainString.run();
        // header
        a.accept(XML.XML_VERSION_1_0_ENCODING_UTF_8);
        a.accept("\n");
        elementToXmlString(document.rootElement(), a, markAsNonPlainString, xmlSpace);
    }

    public static void elementToXmlString(XmlElement element, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        markAsNonPlainString.run();
        // start tag: `<tagname`
        a.accept("<");
        a.accept(element.xmlName().qName());
        // attributes: ` key="value"`
        ObjectRef<XML.XmlSpace> effectiveXmlSpace = objectRef(xmlSpace);

        // AttributeOrder taken from element
        element.attributes().forEach((k, v) -> {
            // inspect for XmlSpace
            if (k.equalsIgnoreCase("xml:space")) {
                switch (v) {
                    case XML.XML_SPACE__PRESERVE:
                        effectiveXmlSpace.value = XML.XmlSpace.preserve;
                        break;
                    case XML.XML_SPACE__DEFAULT:
                        effectiveXmlSpace.value = XML.XmlSpace.default_;
                        break;
                    // in all other cases: leave value as it is
                }
            }
            // serialize
            a.accept(" ");
            a.accept(k);
            a.accept("=");
            a.accept("\"");
            String escapedValue = XmlEscapers.xmlAttributeEscaper().escape(v);
            a.accept(escapedValue);
            a.accept("\"");
        });
        if (element.hasEmptyContent(effectiveXmlSpace.value)) {
            // self-closing tag
            a.accept(" />");
        } else {
            // close start tag
            a.accept(">");

            // content: RECURSE
            contentToXmlString(element, a, markAsNonPlainString, effectiveXmlSpace.value);

            // closing tag
            a.accept("</");
            a.accept(element.xmlName().qName());
            a.accept(">");
        }
    }

    public static void nodeToXmlString(IXmlNode node, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        if (node == null) throw new IllegalArgumentException("Unexpected node null");

        if (node instanceof XmlDocument) {
            XmlDocument document = (XmlDocument) node;
            documentToXmlString(document, a, markAsNonPlainString, xmlSpace);
        } else if (node instanceof XmlElement) {
            XmlElement element = (XmlElement) node;
            elementToXmlString(element, a, markAsNonPlainString, xmlSpace);
        } else if (node instanceof XmlText) {
            XmlText text = (XmlText) node;
            textToXmlString(text, a, markAsNonPlainString, xmlSpace);
        } else if (node instanceof XmlRaw) {
            XmlRaw raw = (XmlRaw) node;
            rawToXmlString(raw, a, markAsNonPlainString, xmlSpace);
        } else {
            throw new IllegalArgumentException("Unexpected node type: " + node.getClass());
        }
    }

    public static void rawToXmlString(XmlRaw xmlRaw, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        markAsNonPlainString.run();
        // append
        a.accept(xmlRaw.raw());
    }

    public static void textSectionToXmlString(XmlText.Section section, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        Boolean escapeContent = null;
        Boolean trimWhitespace = null;
        String text = section.text();

        switch (section.charactersKind()) {
            case Default:
                escapeContent = true;
                // apply context xmlSpace setting and default to trim
                trimWhitespace = xmlSpace != XML.XmlSpace.preserve;
                break;
            case PreserveWhitespace:
                escapeContent = true;
                // section is marked as PRESERVE, so context xmlSpace setting is ignored
                trimWhitespace = false;
                break;
            case IgnorableWhitespace:
                escapeContent = true;
                // section is marked as IGNORABLE, xmlSpace=preserve could keep it nevertheless
                trimWhitespace = xmlSpace != XML.XmlSpace.preserve;
                break;
            case CDATA:
                escapeContent = false;
                // always protect whitespace, so context xmlSpace setting is ignored
                trimWhitespace = false;
                markAsNonPlainString.run();
                text = "<![CDATA[" + text + "]]>";
                break;
        }
        assert trimWhitespace != null;

        if (trimWhitespace) {
            text = XML.normalizeWhitespace(text);
        }
        if (escapeContent) {
            text = XmlEscapers.xmlContentEscaper().escape(text);
        }
        a.accept(text);
    }

    public static void textToXmlString(XmlText xmlText, Consumer<String> a, Runnable markAsNonPlainString, XML.XmlSpace xmlSpace) {
        xmlText.sections().forEach(section -> textSectionToXmlString(section, a, markAsNonPlainString, xmlSpace));
    }

    public static XmlFragmentString toXmlFragmentString(IXmlContent xmlContent, XML.XmlSpace xmlSpace) {
        StringBuilder b = new StringBuilder();
        XmlSerializer.contentToXmlString(xmlContent, b::append, () -> {}, xmlSpace);
        return XmlFragmentString.of(b.toString(), xmlSpace);
    }

    public static Object toXmlString(XmlDocument xmlDocument, XML.XmlSpace xmlSpace) {
        StringBuilder b = new StringBuilder();
        BooleanRef isPlain = new BooleanRef(true);
        XmlSerializer.documentToXmlString(xmlDocument, b::append, () -> isPlain.set(false), xmlSpace);
        if (isPlain.get()) {
            return b.toString();
        } else {
            return XmlFragmentString.of(b.toString(), xmlSpace);
        }
    }

    /**
     * Returns either an {@link XmlFragmentString} or a {@link String}, depending on the contents of this
     * {@link IXmlContent}. An {@link XmlFragmentString} is required (and returned) iff this {@link IXmlContent}
     * contains any node that is an {@link XmlElement}, or if any contained {@link XmlText} contains a
     * {@link XmlText.Section} with {@link CharactersKind#CDATA}.
     */
    public static Object toXmlString(IXmlContent xmlContent, XML.XmlSpace xmlSpace) {
        StringBuilder b = new StringBuilder();
        BooleanRef isPlain = new BooleanRef(true);
        XmlSerializer.contentToXmlString(xmlContent, b::append, () -> isPlain.set(false), xmlSpace);
        if (isPlain.get()) {
            return b.toString();
        } else {
            return XmlFragmentString.of(b.toString(), xmlSpace);
        }
    }

}
