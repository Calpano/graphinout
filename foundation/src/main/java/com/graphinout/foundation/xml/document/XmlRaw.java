package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.XmlWriter;

import java.io.IOException;
import java.util.stream.Stream;

public record XmlRaw(String raw) implements IXmlNode {

    @Override
    public Stream<IXmlNode> directChildren() {
        return Stream.empty();
    }

    @Override
    public void fire(XmlWriter writer) throws IOException {
        writer.raw(raw);
    }

    @Override
    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return raw.isEmpty();
    }

}
