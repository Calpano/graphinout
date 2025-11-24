package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.Xml2StringWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The content of an {@link XmlElement} or {@link XmlDocumentFragment}. Basically, a {@link List} or {@link IXmlNode}.
 */
public class XmlContent implements IXmlContent {

    final List<IXmlNode> children = new ArrayList<>();

    public void addChild(XmlElement child) {
        children.add(child);
    }

    public void addChild(XmlText text) {
        children.add(text);
    }

    public void addChild(XmlRaw raw) {
        children.add(raw);
    }

    /** mutable list, only reorder */
    public List<IXmlNode> childrenList() {
        return this.children;
    }

    // FIXME escaping? raw?
    public String contentAsXml() {
        Xml2StringWriter w = new Xml2StringWriter();
        for (IXmlNode x : children) {
            try {
                x.fire(w);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return w.resultString();
    }

    @Override
    public Stream<IXmlNode> directChildren() {
        return children.stream();
    }


    public void removeChild(XmlElement xmlElement) {
        children.remove(xmlElement);
    }

    /**
     * Whitespace (XML: CR, LF, SPACE, TAB; all included in Javas {@code trim()}) can be removed within each
     * {@link XmlText} node.
     */
    public void removeIgnorableWhitespace(Predicate<XmlElement> contentElementTest) {
        List<XmlElement> list = allElements().filter(elem -> !contentElementTest.test(elem)).toList();
        for (XmlElement nonContentElement : list) {
            // first, simplify TextNodes
            nonContentElement.directChildren().filter(node -> node instanceof XmlText).map(node -> (XmlText) node).forEach(XmlText::removeIgnorableWhitespace);
            // then remove empty text nodes
            nonContentElement.removeEmptyTextNodes();
        }
    }


    @Override
    public String toString() {
        return "xmlContent{" + directChildren().map(Object::toString).collect(Collectors.joining(", ")) + "}";
    }

    void removeEmptyTextNodes() {
        children.removeIf(node -> node instanceof XmlText textNode && textNode.hasEmptyContent(XML.XmlSpace.default_));
    }

}
