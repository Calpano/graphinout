package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class XmlElement extends XmlNode {

    @FunctionalInterface
    public interface AttributeTest {

        boolean test(XmlElement element, String attName, String attValue);

    }

    /** we keep order, but in general SAX parsers have NO ORDER in their attributes */
    final Map<String, String> attributes = new LinkedHashMap<>();
    final IXmlName xmlName;
    final List<XmlNode> children = new ArrayList<>();
    final @Nullable XmlElement parent;

    /**
     * @param parent root element has no parent element
     */
    public XmlElement(@Nullable XmlElement parent, IXmlName xmlName, Map<String, String> attributes) {
        this.parent = parent;
        this.xmlName = xmlName;
        this.attributes.putAll(attributes);
    }

    public void addChild(XmlElement child) {
        children.add(child);
    }

    public void addChild(XmlText text) {
        children.add(text);
    }

    public void addChild(XmlRaw raw) {
        children.add(raw);
    }

    /** get attribute */
    public @Nullable String attribute(String attName) {
        return attributes.get(attName);
    }

    public @NonNull String attribute_(String attName) {
        String value = attributes.get(attName);
        assert value != null : "attribute '" + attName + "' not found in "+this;
        return value;
    }

    /**
     * @param attName  to update
     * @param valueFun may receive null (att was not present) or return null (remove att). Other values are updating.
     */
    public void attributeUpdate(String attName, Function<@Nullable String, @Nullable String> valueFun) {
        String currentValue = attribute(attName);
        String newValue = valueFun.apply(currentValue);

        if (currentValue == null) {
            attributes.put(attName, newValue);
        } else {
            if (newValue == null) {
                attributes.remove(attName);
            } else {
                attributes.put(attName, newValue);
            }
        }
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public void changeIfPresent(@NonNull String key, @NonNull String searchValue, @NonNull String replaceValue) {
        String currentValue = attribute(key);
        if (Objects.equals(currentValue, searchValue)) {
            attributes.put(key, replaceValue);
        }
    }

    public void changeIfPresent(@NonNull String key, Function<String, String> valueFun) {
        String currentValue = attribute(key);
        if (currentValue == null) return;
        String newValue = valueFun.apply(currentValue);
        if (newValue == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, newValue);
        }
    }

    /** mutable list, only reorder */
    public List<XmlNode> childrenList() {
        return this.children;
    }

    public String contentAsXml() {
        Xml2StringWriter w = new Xml2StringWriter();
        for(XmlNode x : children) {
            try {
                x.fire(w);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return w.resultString();
    }

    @Override
    public Stream<XmlNode> directChildren() {
        return children.stream();
    }

    public void fire(XmlWriter writer) throws IOException {
        writer.elementStart(xmlName, attributes);
        for (XmlNode node : children) {
            node.fire(writer);
        }
        writer.elementEnd(xmlName);
    }

    public String localName() {
        return xmlName.localName();
    }

    public @Nullable XmlElement parent() {
        return parent;
    }

    public String qName() {
        return xmlName.qName();
    }

    public void removeAttributeIf(AttributeTest attributeTest) {
        List<String> toRemove = new ArrayList<>();
        attributes.forEach((attName, attValue) -> {
            boolean remove = attributeTest.test(this, attName, attValue);
            if (remove) {
                toRemove.add(attName);
            }
        });
        toRemove.forEach(attributes::remove);
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
        return "<" + xmlName.localName() + " atts=" + attributes + '>';
    }

    /** just the namespace URI */
    public String uri() {
        return xmlName.uri();
    }

    public IXmlName xmlName() {
        return xmlName;
    }

    private void removeEmptyTextNodes() {
        children.removeIf(node -> node instanceof XmlText textNode && textNode.isEmpty());
    }

}
