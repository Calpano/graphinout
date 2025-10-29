package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.util.PowerStreams;
import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.IXmlName;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.XmlWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class XmlElement extends XmlContent implements IXmlNode {

    @FunctionalInterface
    public interface AttributeTest {

        boolean test(XmlElement element, String attName, String attValue);

    }

    /** we keep order, but in general SAX parsers have NO ORDER in their attributes */
    final Map<String, String> attributes = new LinkedHashMap<>();
    final IXmlName xmlName;
    final @Nullable XmlElement parent;

    /**
     * @param parent root element has no parent element
     */
    public XmlElement(@Nullable XmlElement parent, IXmlName xmlName, Map<String, String> attributes) {
        this.parent = parent;
        this.xmlName = xmlName;
        this.attributes.putAll(attributes);
    }

    public void addIfNotPresent(@NonNull String key, String value) {
        attributes.putIfAbsent(key, value);
    }

    /** get attribute */
    public @Nullable String attribute(String attName) {
        return attributes.get(attName);
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

    public @NonNull String attribute_(String attName) {
        String value = attributes.get(attName);
        assert value != null : "attribute '" + attName + "' not found in " + this;
        return value;
    }

    public Map<String, String> attributes() {
        return attributes;
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

    public void fire(XmlWriter writer) throws IOException {
        writer.elementStart(xmlName, attributes);
        for (IXmlNode node : children) {
            node.fire(writer);
        }
        writer.elementEnd(xmlName);
    }

    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return super.hasEmptyContent(XML.XmlSpace.default_);
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

    public void removeAttribute(String attName) {
        attributes.remove(attName);
    }

    public void removeChild(XmlElement xmlElement) {
        children.remove(xmlElement);
    }

    public void setContent(String text, CharactersKind charactersKind) {
        // remove all text nodes
        children.removeIf(node -> node instanceof XmlText);
        // add this as text node
        children.add(XmlText.of(text, charactersKind));
    }

    /**
     * Removes all non-element children!
     * @param sortKeyFun
     */
    public void sortChildElementsBySortKey(Function<XmlElement, String> sortKeyFun) {
        List<XmlElement> childElements = new ArrayList<>();
        PowerStreams.filterMap(children.stream(), XmlElement.class).forEach(childElements::add);
        childElements.sort( Comparator.comparing(sortKeyFun));
        children.clear();
        children.addAll(childElements);
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

}
