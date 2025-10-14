package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlSerializer;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IXmlContent {

    default Stream<XmlElement> allElements() {
        return allNodes().filter(node -> node instanceof XmlElement).map(node -> (XmlElement) node);
    }

    /** including this */
    default Stream<IXmlContent> allNodes() {
        return Stream.concat(Stream.of(this), directChildren().flatMap(IXmlNode::allNodes));
    }

    Stream<IXmlNode> directChildren();

    default void forEachMatchingElement(Predicate<XmlElement> test, Consumer<XmlElement> action) {
        allNodes().filter(node -> node instanceof XmlElement).map(node -> (XmlElement) node).filter(test)
                // buffer
                .toList()
                .forEach(action);
    }

    default boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return directChildren().allMatch(node -> {
            switch (node) {
                // for text nodes, empty content is ok
                case XmlText xmlText -> {
                    return xmlText.hasEmptyContent(xmlSpace);
                }
                case XmlRaw xmlRaw -> {
                    return xmlRaw.hasEmptyContent(xmlSpace);
                }
                // all other cases: that is not empty
                default -> {
                    return false;
                }
            }
        });
    }

    default XmlFragmentString toXmlFragmentString() {
        return XmlSerializer.toXmlFragmentString(this, XML.XmlSpace.default_);
    }

    /**
     * Returns either an {@link XmlFragmentString} or a {@link String}, depending on the contents of this
     * {@link IXmlContent}. An {@link XmlFragmentString} is required (and returned) iff this {@link IXmlContent}
     * contains any node that is an {@link XmlElement}, or if any contained {@link XmlText} contains a
     * {@link XmlText.Section} with {@link CharactersKind#CDATA}.
     */
    default Object toXmlString() {
        return XmlSerializer.toXmlString(this, XML.XmlSpace.default_);
    }


}
