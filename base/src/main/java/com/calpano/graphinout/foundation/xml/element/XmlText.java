package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;

public class XmlText extends XmlNode {

    String text;

    public XmlText(String text) {
        this.text = text;
    }

    @Override
    void fire(XmlWriter writer) throws IOException {
        writer.characterDataWhichMayContainCdata(text);
    }

}
