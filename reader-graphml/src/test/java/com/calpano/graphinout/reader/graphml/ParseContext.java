package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;

class ParseContext {

    /** we use a LinkedList so we can peek deeper */
    private final LinkedList<ElementContext> elementStack = new LinkedList<>();
    private XmlMode mode = XmlMode.Graphml;

    public boolean isEmpty() {
        return elementStack.isEmpty();
    }

    public boolean isInterpretedAsGraphml() {
        return mode != XmlMode.PCDATA;
    }

    public boolean isInterpretedAsPCDATA() {
        return mode == XmlMode.PCDATA;
    }

    public void mode(XmlMode xmlMode) {
        this.mode = xmlMode;
    }

    public @Nullable ElementContext peekNullable() {
        return elementStack.peek();
    }

    public ElementContext peek_(String... expectedNames) {
        ElementContext context = elementStack.peek();
        assert context != null;
        assert expectedNames.length == 0 || Set.of(expectedNames).contains(context.elementName) : "Expected element '" + Set.of(expectedNames) + "' but got '" + context.elementName + "'";
        return context;
    }

    public ElementContext pop(String expectedName) {
        assert !elementStack.isEmpty() : "Element stack is empty at pop";
        ElementContext context = elementStack.pop();
        assert context.elementName.equals(expectedName) : "Expected element '" + expectedName + "' but got '" + context.elementName + "'";
        return context;
    }

    public ElementContext push(String elementName, Map<String, String> attributes, boolean isRawXml, GraphmlElementBuilder<?> builder, XmlMode xmlMode) {
        ElementContext context = new ElementContext(peekNullable(), elementName, attributes, isRawXml, builder);
        elementStack.push(context);
        this.mode = xmlMode;
        return context;
    }

    @Nullable
    GraphmlGraphBuilder findParentGraphElement() {
        // dig in stack to find the parent Graph element
        for (int i = elementStack.size() - 1; i >= 0; i--) {
            ElementContext context = elementStack.get(i);
            if (context.elementName.equals(GRAPH)) {
                return context.graphBuilder();
            }
        }
        return null;
    }

}
