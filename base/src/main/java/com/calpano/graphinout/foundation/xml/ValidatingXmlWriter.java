package com.calpano.graphinout.foundation.xml;

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
    public void elementEnd(String name) throws IOException {
        String expected = elements.pop();
        if (expected == null || !expected.equals(name)) {
            throw new IllegalStateException("XML nesting: Expected close of element '" + expected + "' but got '" + name + "'. Stack: " + this.elements);
        }
        super.elementEnd(name);
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        elements.push(name);
        super.elementStart(name, attributes);
    }

}
