package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.stream.Stream;

public class XmlRaw extends XmlNode {

    private final String raw;

    public XmlRaw(String raw) {
        this.raw = raw;
    }

    @Override
    public Stream<XmlNode> directChildren() {
        return Stream.empty();
    }

    @Override
    public void fire(XmlWriter writer) throws IOException {
        writer.raw(raw);
    }

}
