package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.xml.XmlWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

/**
 * A chaining, validating {@link XmlWriter}.
 */
@Slf4j
public class ValidatingXmlWriter implements XmlWriter {

    protected final XmlWriter sink;
    private final Stack<String> elements = new Stack<>();

    public ValidatingXmlWriter(XmlWriter sink) {
        this.sink = sink;
    }

    @Override
    public void characterData(String characterData) throws IOException {
        sink.characterData(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        sink.endDocument();
    }

    @Override
    public void endElement(String name) throws IOException {
        String expected = elements.pop();
        if (expected == null || !expected.equals(name)) {
            throw new IllegalStateException("Expected close of element " + expected + " but got " + name);
        }
        sink.endElement(name);
    }

    @Override
    public void startDocument() throws IOException {
        sink.startDocument();
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        elements.push(name);
        sink.startElement(name, attributes);
    }

}
