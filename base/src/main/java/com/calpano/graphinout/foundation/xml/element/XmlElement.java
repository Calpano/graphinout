package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmlElement extends XmlNode {

    /** we keep order, but in general SAX parsers have NO ORDER in their attributes */
    final Map<String, String> attributes = new LinkedHashMap<>();
    final IXmlName xmlName;
    final List<XmlNode> children = new ArrayList<>();

    public XmlElement(IXmlName xmlName, Map<String, String> attributes) {
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

    public void fire(XmlWriter writer) throws IOException {
        writer.elementStart(xmlName, attributes);
        for (XmlNode node : children) {
            node.fire(writer);
        }
        writer.elementEnd(xmlName);
    }


}
