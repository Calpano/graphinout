package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class XmlNode {

    public Stream<XmlElement> allElements() {
        return allNodes().filter(node -> node instanceof XmlElement).map(node -> (XmlElement) node);
    }

    /** including this */
    public Stream<XmlNode> allNodes() {
        return Stream.concat(Stream.of(this), directChildren().flatMap(XmlNode::allNodes));
    }

    public abstract Stream<XmlNode> directChildren();

    public abstract void fire(XmlWriter writer) throws IOException;

    public void forEachMatchingElement(Predicate<XmlElement> test, Consumer<XmlElement> action) {
        allNodes().filter(node -> node instanceof XmlElement).map(node -> (XmlElement) node).filter(test).forEach(action);
    }

}
