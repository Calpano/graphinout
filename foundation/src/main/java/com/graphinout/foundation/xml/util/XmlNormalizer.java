package com.graphinout.foundation.xml.util;

import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.document.IXmlNode;
import com.graphinout.foundation.xml.document.Xml2DocumentWriter;
import com.graphinout.foundation.xml.document.XmlDocument;
import com.graphinout.foundation.xml.document.XmlElement;
import com.graphinout.foundation.xml.writer.Xml2StringWriter;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Parse XML into DOM, apply a  series of mutations, write back.
 */

@SuppressWarnings("UnusedReturnValue")
public class XmlNormalizer {

    private final XmlDocument doc;
    private XML.AttributeOrderPerElement attributeOrder = XML.AttributeOrderPerElement.AsWritten;

    public XmlNormalizer(String xml) {
        try {
            this.doc = Xml2DocumentWriter.parseToDoc(xml);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse/normalize XML=----\n" + xml + "\n----", e);
        } catch (IOException e) {
            throw new RuntimeException("IO in parse XML", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Canonicalize. Element order is strict and kept.
     * <li>Comments are stripped.</li>
     * <li>Attribute order is sorted alphabetically.</li>
     * <li>Does not preserve whitespace.</li>
     */
    public static String normalize(String xml) {
        return new XmlNormalizer(xml) //
                .sortAttributesLexicographically() //
                .normalizeMultipleSpacesInXsiAttributeValue() //
                .removeIgnorableWhitespace(elem -> false) //
                .resultXmlString();
    }

    public XmlDocument doc() {
        return doc;
    }

    public XmlNormalizer normalizeMultipleSpacesInXsiAttributeValue() {
        // replace all multiple spaces with single space
        doc().rootElement().changeIfPresent(XML.ATT_XSI_SCHEMA_LOCATION, val -> //
                val.replaceAll("\\s+", " ").trim());
        return this;
    }

    public void removeAttributeIf(XmlElement.AttributeTest attributeTest) {
        doc.allNodes().filter(node -> node instanceof XmlElement).forEach(elem -> {
            XmlElement xmlElement = (XmlElement) elem;
            xmlElement.removeAttributeIf(attributeTest);
        });
    }

    public void removeElementIf(Predicate<XmlElement> test) {
        // traverse doc BFS
        List<IXmlNode> current = new ArrayList<>(doc.directChildren().toList());
        while (!current.isEmpty()) {
            for (IXmlNode node : current) {
                if (node instanceof XmlElement xmlElement && test.test(xmlElement)) {
                    xmlElement.parent().removeChild(xmlElement);
                }
            }
            current = new ArrayList<>(current.stream().flatMap(IXmlNode::directChildren).toList());
        }
    }

    /**
     * @param contentElementTest in which whitespace needs to be preserved
     * @return
     */
    public XmlNormalizer removeIgnorableWhitespace(Predicate<XmlElement> contentElementTest) {
        doc.rootElement().removeIgnorableWhitespace(contentElementTest);
        return this;
    }

    public String resultXmlString() {
        Xml2StringWriter xmlWriter = new Xml2StringWriter(attributeOrder, true, null);
        try {
            doc.fire(xmlWriter);
            return xmlWriter.resultString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlNormalizer sortAttributesLexicographically() {
        attributeOrder = XML.AttributeOrderPerElement.Lexicographic;
        return this;
    }


}
