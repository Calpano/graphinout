package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.XmlWriter;

import java.io.IOException;
import java.util.stream.Stream;

public class XmlDocument implements IXmlNode {

    private XmlElement root;

    @Override
    public Stream<IXmlNode> directChildren() {
        return Stream.of(root);
    }

    public void fire(XmlWriter writer) throws IOException {
        writer.documentStart();
        root.fire(writer);
        writer.documentEnd();
    }

    @Override
    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return false;
    }

    public XmlElement rootElement() {
        return root;
    }

    public void setRoot(XmlElement rootElement) {
        assert root == null;
        root = rootElement;
    }

}
