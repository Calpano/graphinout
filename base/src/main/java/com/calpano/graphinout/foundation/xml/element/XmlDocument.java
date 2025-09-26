package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;

public class XmlDocument extends XmlNode {

    XmlElement root;

    public void fire(XmlWriter writer) throws IOException {
        writer.documentStart();
        root.fire(writer);
        writer.documentEnd();
    }

    public void setRoot(XmlElement rootElement) {
        assert root == null;
        root = rootElement;
    }

}
