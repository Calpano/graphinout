package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.writer.XmlWriter;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

public final class XmlRaw implements IXmlNode {

    private final String raw;

    public XmlRaw(String raw) {this.raw = raw;}

    @Override
    public Stream<IXmlNode> directChildren() {
        return Stream.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        XmlRaw that = (XmlRaw) obj;
        return Objects.equals(this.raw, that.raw);
    }

    @Override
    public void fire(XmlWriter writer) throws IOException {
        writer.raw(raw);
    }

    @Override
    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return raw.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    public String raw() {return raw;}

    @Override
    public String toString() {
        return "XmlRaw[" + "raw=" + raw + ']';
    }

}
