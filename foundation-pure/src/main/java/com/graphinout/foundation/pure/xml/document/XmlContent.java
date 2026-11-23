package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;

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

    /**
     * The children as an XML fragment (no XML declaration), so that parsing the result gives back this content:
     * <ul>
     *     <li>{@link XmlText} is escaped ({@code & < > " '}); its CDATA sections are written as CDATA, unescaped.</li>
     *     <li>{@link XmlRaw} is written as-is, without any check. It must already be well-formed XML.</li>
     *     <li>{@link XmlElement}s are written with attributes sorted by name (see {@link Xml2StringWriter#Xml2StringWriter()}).</li>
     * </ul>
     * Tested in {@code XmlContentTest}.
     */
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
        List<XmlElement> list = Java9.Stream.toList(allElements().filter(elem -> !contentElementTest.test(elem)));
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
        children.removeIf(node -> {
            if (node instanceof XmlText) {
                XmlText textNode = (XmlText) node;
                return textNode.hasEmptyContent(XML.XmlSpace.default_);
            } else {
                return false;
            }
        });
    }

}
