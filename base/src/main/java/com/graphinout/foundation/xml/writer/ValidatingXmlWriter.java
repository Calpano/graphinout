package com.graphinout.foundation.xml.writer;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

/**
 * A chaining, validating {@link XmlWriter}.
 */
public class ValidatingXmlWriter extends DelegatingXmlWriter {

    private final Stack<String> elements = new Stack<>();

    public ValidatingXmlWriter(XmlWriter sink) {
        super(sink);
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        String expected = elements.pop();
        if (expected == null || !expected.equals(localName)) {
            throw new IllegalStateException("XML nesting: Expected close of element '" + expected + "' but got '" + localName + "'. Stack: " + this.elements);
        }
        super.elementEnd(uri, localName, qName);
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        elements.push(localName);
        super.elementStart(uri, localName, qName, attributes);
    }

}
