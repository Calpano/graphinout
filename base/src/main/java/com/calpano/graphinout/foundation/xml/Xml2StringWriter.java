package com.calpano.graphinout.foundation.xml;

public class Xml2StringWriter extends Xml2AppendableWriter {

    public Xml2StringWriter() {
        super(new StringBuilder());
    }

    public void reset() {
        buffer().setLength(0);
    }

    public String string() {
        return buffer().toString();
    }

    public String toString() {
        return buffer().toString();
    }

    private StringBuilder buffer() {
        return (StringBuilder) appendable;
    }

}
