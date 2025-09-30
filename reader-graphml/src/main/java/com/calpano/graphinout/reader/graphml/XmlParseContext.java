package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.foundation.xml.IXmlName;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;

/** Used for parsing XML as GraphML */
public class XmlParseContext {

    /** we use a LinkedList so we can peek deeper */
    private final LinkedList<XmlElementContext> elementStack = new LinkedList<>();
    private XmlMode mode = XmlMode.Graphml;

    public boolean isEmpty() {
        return elementStack.isEmpty();
    }

    public boolean isInterpretedAsGraphml() {
        return mode != XmlMode.GENERIC_PC_DATA;
    }

    /** i.e. XML is not interpreted as GraphML here */
    public boolean isInterpretedAsGenericPCDATA() {
        return mode == XmlMode.GENERIC_PC_DATA;
    }

    public void mode(XmlMode xmlMode) {
        this.mode = xmlMode;
    }

    public @Nullable XmlElementContext peekNullable() {
        return elementStack.peek();
    }

    public XmlElementContext peek_(String... expectedNames) {
        XmlElementContext context = elementStack.peek();
        assert context != null;
        assert expectedNames.length == 0 || Set.of(expectedNames).contains(context.xmlElementName) : "Expected element '" + Set.of(expectedNames) + "' but got '" + context.xmlElementName + "'";
        return context;
    }

    public XmlElementContext pop(String expectedLocalName) {
        assert !elementStack.isEmpty() : "Element stack is empty at pop";
        XmlElementContext context = elementStack.pop();
        assert context.xmlElementName.equals(expectedLocalName) : "Expected element '" + expectedLocalName + "' but got '" + context.xmlElementName + "'";
        return context;
    }

    public XmlElementContext push(IXmlName elementName, Map<String, String> attributes, boolean isRawXml, GraphmlElementBuilder<?> builder, XmlMode xmlMode) {
        XmlElementContext context = new XmlElementContext(peekNullable(), elementName, attributes, isRawXml, builder);
        elementStack.push(context);
        this.mode = xmlMode;
        return context;
    }

    @Nullable
    GraphmlGraphBuilder findParentGraphElement() {
        // dig in stack to find the parent Graph element
        for (int i = elementStack.size() - 1; i >= 0; i--) {
            XmlElementContext context = elementStack.get(i);
            if (context.xmlElementName.equals(GRAPH)) {
                return context.graphBuilder();
            }
        }
        return null;
    }

}
