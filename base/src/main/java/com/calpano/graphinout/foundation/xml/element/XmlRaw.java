package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;

public class XmlRaw extends XmlNode {

    String raw;

    public XmlRaw(String raw) {
        this.raw = raw;
    }

    @Override
    void fire(XmlWriter writer) throws IOException {
        writer.raw(raw);
    }

}
