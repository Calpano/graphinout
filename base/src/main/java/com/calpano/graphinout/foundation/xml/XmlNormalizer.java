package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.xml.element.Xml2DocumentWriter;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
import com.calpano.graphinout.foundation.xml.element.XmlElement;
import com.calpano.graphinout.foundation.xml.element.XmlNode;
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
    private Xml2AppendableWriter.AttributeOrderPerElement attributeOrder = Xml2AppendableWriter.AttributeOrderPerElement.AsWritten;

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

    public static String normalize(String xml) {
        return new XmlNormalizer(xml).sortAttributesLexicographically().toXmlString();
    }

    public XmlDocument doc() {
        return doc;
    }

    public void normalizeMultipleSpacesInXsiAttributeValue() {
        // replace all multiple spaces with single space
        doc().rootElement().changeIfPresent(XML.ATT_XSI_SCHEMA_LOCATION, val -> //
                val.replaceAll("\\s+", " "));
    }

    public void removeAttributeIf(XmlElement.AttributeTest attributeTest) {
        doc.allNodes().filter(node -> node instanceof XmlElement).forEach(elem -> {
            XmlElement xmlElement = (XmlElement) elem;
            xmlElement.removeAttributeIf(attributeTest);
        });
    }

    public void removeElementIf(Predicate<XmlElement> test) {
        // traverse doc BFS
        List<XmlNode> current = new ArrayList<>(doc.directChildren().toList());
        while (!current.isEmpty()) {
            for(XmlNode node : current) {
                if(node instanceof XmlElement xmlElement && test.test(xmlElement)) {
                    xmlElement.parent().removeChild(xmlElement);
                }
            }
            current = new ArrayList<>(current.stream().flatMap(XmlNode::directChildren).toList());
        }
    }

    /**
     * @param contentElementTest in which whitespace needs to be preserved
     */
    public void removeIgnorableWhitespace(Predicate<XmlElement> contentElementTest) {
        doc.rootElement().removeIgnorableWhitespace(contentElementTest);
    }

    public XmlNormalizer sortAttributesLexicographically() {
        attributeOrder = Xml2AppendableWriter.AttributeOrderPerElement.Lexicographic;
        return this;
    }

    public String toXmlString() {
        Xml2StringWriter xmlWriter = new Xml2StringWriter(attributeOrder, true);
        try {
            doc.fire(xmlWriter);
            return xmlWriter.resultString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
