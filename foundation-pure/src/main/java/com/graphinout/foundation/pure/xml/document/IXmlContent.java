package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.xml.CharactersKind;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import com.graphinout.foundation.pure.xml.XmlSerializer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * XML content holding child nodes, with traversal over descendant nodes and elements.
 */
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
        // buffer
        Java9.Stream.toList(allNodes().filter(node -> node instanceof XmlElement).map(node -> (XmlElement) node).filter(test)).forEach(action);
    }

    default boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return directChildren().allMatch(node -> {
            // for text nodes, empty content is ok
            if (Objects.requireNonNull(node) instanceof XmlText) {
                XmlText xmlText = (XmlText) node;
                return xmlText.hasEmptyContent(xmlSpace);
            } else if (node instanceof XmlRaw) {
                XmlRaw xmlRaw = (XmlRaw) node;
                return xmlRaw.hasEmptyContent(xmlSpace);
                // all other cases: that is not empty
            }
            return false;
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
