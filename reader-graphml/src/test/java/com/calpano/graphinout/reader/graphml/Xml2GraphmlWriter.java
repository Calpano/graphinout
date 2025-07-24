package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.impl.GraphmlDocument;
import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlEndpoint;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlKey;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.impl.GraphmlNode;
import com.calpano.graphinout.base.graphml.impl.GraphmlPort;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Xml2GraphmlWriter implements XmlWriter {

    enum Mode {Graphml, Data}

    // Helper class to track element context
    private static class ElementContext {

        final String elementName;
        final Map<String, String> attributes;
        final boolean isRawXml;
        public GraphmlHyperEdge.GraphmlHyperEdgeBuilder hyperEdgeBuilder;
        boolean containsRawXml;
        // GraphML objects
        GraphmlDocument document;
        GraphmlGraph graph;
        GraphmlNode node;
        IGraphmlEndpoint endpoint;
        IGraphmlPort port;
        GraphmlKey key;
        // Data element specific
        String dataKey;

        ElementContext(String elementName, Map<String, String> attributes, boolean isRawXml) {
            this.elementName = elementName;
            this.attributes = attributes;
            this.isRawXml = isRawXml;
        }

    }

    private final GraphmlWriter graphmlWriter;
    private final LinkedList<ElementContext> elementStack = new LinkedList<>();
    private final StringBuilder characterBuffer = new StringBuilder();
    private final Xml2AppendableWriter rawXmlWriter = new Xml2AppendableWriter(characterBuffer);
    private Mode mode = Mode.Graphml;

    public Xml2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        if (mode == Mode.Data) {
            rawXmlWriter.characterData(characterData, false);
        } else {
            // Accumulate character data for elements that need it (like data, desc, default)
            characterBuffer.append(characterData);
        }
    }

    @Override
    public void endCDATA() throws IOException {
        if (mode == Mode.Data) {
            rawXmlWriter.endCDATA();
        } else {
            // TODO CDATA handling - treat as character data
            throw new UnsupportedOperationException("not implemented yet");
        }
    }

    @Override
    public void endDocument() throws IOException {
        graphmlWriter.endDocument();
    }

    @Override
    public void endElement(String name) throws IOException {
        switch (name) {
            case GraphmlElements.GRAPHML -> endGraphmlElement();
            case GraphmlElements.GRAPH -> endGraphElement();
            case GraphmlElements.NODE -> endNodeElement();
            case GraphmlElements.EDGE -> endEdgeElement();
            case GraphmlElements.HYPER_EDGE -> endHyperEdgeElement();
            case GraphmlElements.PORT -> endPortElement();
            case GraphmlElements.KEY -> endKeyElement();
            case GraphmlElements.DATA -> endDataElement();
            case GraphmlElements.DESC -> endDescElement();
            case GraphmlElements.DEFAULT -> endDefaultElement();
            case GraphmlElements.LOCATOR -> endLocatorElement();
            case GraphmlElements.ENDPOINT -> endEndpointElement();
            default -> {
                if (mode == Mode.Data) {
                    rawXmlWriter.endElement(name);
                    assert !elementStack.isEmpty() : "Element stack is empty at END " + name;
                    ElementContext context = elementStack.pop();
                    assert context.elementName.equals(name);

                    if (!elementStack.isEmpty()) {
                        ElementContext top = elementStack.peek();
                        if (top.elementName.equals(GraphmlElements.DATA) && !top.isRawXml) {
                            top.containsRawXml = true;
                        }
                    }
                } else {
                    throw new IllegalStateException("Unknown element: " + name);
                }
            }
        }
        if (mode == Mode.Graphml) {
            // Clear character buffer after processing element
            characterBuffer.setLength(0);
        }
    }

    @Override
    public void lineBreak() {
        // Line breaks are typically ignored in GraphML processing
    }

    @Override
    public void raw(String rawXml) throws IOException {
        if (mode == Mode.Data) {
            rawXmlWriter.raw(rawXml);
        } else {
            throw new UnsupportedOperationException("no raw XML in GraphML");
        }
    }

    @Override
    public void startCDATA() throws IOException {
        if (mode == Mode.Data) {
            rawXmlWriter.startCDATA();
        } else {
            throw new UnsupportedOperationException("not implemented yet");
        }
    }

    @Override
    public void startDocument() {
        // Document will be started when we encounter the graphml element
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        switch (name) {
            case GraphmlElements.GRAPHML -> startGraphmlElement(attributes);
            case GraphmlElements.GRAPH -> startGraphElement(attributes);
            case GraphmlElements.NODE -> startNodeElement(attributes);
            case GraphmlElements.EDGE -> startEdgeElement(attributes);
            case GraphmlElements.HYPER_EDGE -> startHyperEdgeElement(attributes);
            case GraphmlElements.PORT -> startPortElement(attributes);
            case GraphmlElements.KEY -> startKeyElement(attributes);
            case GraphmlElements.DATA -> startDataElement(attributes);
            case GraphmlElements.DESC -> startDescElement(attributes);
            case GraphmlElements.DEFAULT -> startDefaultElement(attributes);
            case GraphmlElements.LOCATOR -> startLocatorElement(attributes);
            case GraphmlElements.ENDPOINT -> startEndpointElement(attributes);
            default -> {
                if (mode == Mode.Data) {
                    // push to stack and emit to rawXmlBuffer
                    ElementContext context = new ElementContext(name, attributes, true);
                    elementStack.push(context);
                    this.rawXmlWriter.startElement(name, attributes);
                } else {
                    throw new IllegalStateException("Unknown element: " + name);
                }
            }
        }
    }

    private void endDataElement() throws IOException {
        if (!elementStack.isEmpty()) {
            ElementContext context = elementStack.pop();
            GraphmlData.GraphmlDataBuilder builder = GraphmlData.builder();

            if (context.dataKey != null) {
                builder.key(context.dataKey);
            }
            builder.attributes(context.attributes);

            String content = characterBuffer.toString();//.trim();
            if (!content.isEmpty()) {
                if (context.containsRawXml) {
                    builder.valueRaw(content);
                } else {
                    builder.value(content);
                }
            }

            IGraphmlData data = builder.build();
            graphmlWriter.data(data);
            mode = Mode.Graphml;
        }
    }

    private void endDefaultElement() {
        if (!elementStack.isEmpty()) {
            elementStack.pop();
            // Default content is handled by the parent element (key)
        }
    }

    private void endDescElement() {
        assert !elementStack.isEmpty();
        elementStack.pop();
        // Set description for the parent element
        ElementContext parentContext = elementStack.peek();
        if (parentContext != null) {
            String desc = characterBuffer.toString().trim();
            GraphmlDescription gDesc = GraphmlDescription.builder().value(desc).build();
            switch (parentContext.elementName) {
                case GraphmlElements.GRAPHML -> parentContext.document.setDesc(gDesc);
                case GraphmlElements.GRAPH -> parentContext.graph.setDesc(gDesc);
                case GraphmlElements.NODE -> parentContext.node.setDesc(gDesc);
                case GraphmlElements.EDGE, GraphmlElements.HYPER_EDGE -> parentContext.hyperEdgeBuilder.desc(gDesc);
                case GraphmlElements.KEY -> parentContext.key.setDesc(gDesc);
            }
        }


        if (!elementStack.isEmpty()) {
            // Description content is handled by the parent element -- not true -- bug
        }
    }

    private void endEdgeElement() throws IOException {
        assert !elementStack.isEmpty();

        ElementContext context = elementStack.pop();
        assert context.elementName.equals(GraphmlElements.EDGE);

        graphmlWriter.endEdge();
    }

    private void endEndpointElement() {
        if (!elementStack.isEmpty()) {
            ElementContext endpointContext = elementStack.pop();
            // Add the endpoint to the parent hyperedge builder
            if (!elementStack.isEmpty() && endpointContext.endpoint != null) {
                ElementContext parentContext = elementStack.peek();
                if (parentContext.hyperEdgeBuilder != null) {
                    parentContext.hyperEdgeBuilder.addEndpoint(endpointContext.endpoint);
                }
            }
        }
    }

    private void endGraphElement() throws IOException {
        if (!elementStack.isEmpty()) {
            ElementContext context = elementStack.pop();
            graphmlWriter.endGraph(context.graph != null ? context.graph.getLocator() : null);
        }
    }

    private void endGraphmlElement() {
        if (!elementStack.isEmpty()) {
            elementStack.pop();
        }
    }

    private void endHyperEdgeElement() throws IOException {
        if (!elementStack.isEmpty()) {
            ElementContext context = elementStack.pop();
            if (context.hyperEdgeBuilder != null) {
                GraphmlHyperEdge hyperEdge = context.hyperEdgeBuilder.build();
                graphmlWriter.startHyperEdge(hyperEdge);
            }
        }
        graphmlWriter.endHyperEdge();
    }

    private void endKeyElement() throws IOException {
        if (!elementStack.isEmpty()) {
            ElementContext context = elementStack.pop();
            if (context.key != null) {
                graphmlWriter.key(context.key);
            }
        }
    }

    private void endLocatorElement() {
        if (!elementStack.isEmpty()) {
            elementStack.pop();
            // Locator is handled by the parent element
        }
    }

    private void endNodeElement() throws IOException {
        if (!elementStack.isEmpty()) {
            ElementContext context = elementStack.pop();
            graphmlWriter.endNode(context.node != null ? context.node.locator() : null);
        }
    }

    private void endPortElement() throws IOException {
        if (!elementStack.isEmpty()) {
            elementStack.pop();
        }
        graphmlWriter.endPort();
    }

    private @Nullable IGraphmlGraph findParentGraphElement() {
        // dig in stack to find the parent Graph element
        for (int i = elementStack.size() - 1; i >= 0; i--) {
            ElementContext context = elementStack.get(i);
            if (context.elementName.equals(GraphmlElements.GRAPH)) {
                return context.graph;
            }
        }
        return null;
    }

    private void startDataElement(Map<String, String> attributes) {
        ElementContext context = new ElementContext(GraphmlElements.DATA, attributes, false);
        if (attributes != null) {
            context.dataKey = attributes.get("key");
        }
        elementStack.push(context);
        this.mode = Mode.Data;
    }

    private void startDefaultElement(Map<String, String> attributes) {
        ElementContext context = new ElementContext(GraphmlElements.DEFAULT, attributes, false);
        elementStack.push(context);
    }

    private void startDescElement(Map<String, String> attributes) {
        ElementContext context = new ElementContext(GraphmlElements.DESC, attributes, false);
        elementStack.push(context);
    }

    private void startEdgeElement(Map<String, String> attributes) throws IOException {
        ElementContext context = new ElementContext(GraphmlElements.EDGE, attributes, false);
        String edgeId = attributes.get(IGraphmlEdge.ATTRIBUTE_ID);
        context.hyperEdgeBuilder = IGraphmlHyperEdge.builder(edgeId);
        context.hyperEdgeBuilder.attributes(attributes);

        boolean isDirected = requireNonNull(findParentGraphElement()).isDirectedEdges();
        @Nullable String b = attributes.get(IGraphmlEdge.ATTRIBUTE_DIRECTED);
        if (b != null) {
            isDirected = Boolean.parseBoolean(b);
        }

        GraphmlDirection sourceDir = isDirected ? GraphmlDirection.In : GraphmlDirection.Undirected;
        context.hyperEdgeBuilder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE_PORT)) //
                .type(sourceDir) //
                .build());
        GraphmlDirection targetDir = isDirected ? GraphmlDirection.Out : GraphmlDirection.Undirected;
        context.hyperEdgeBuilder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET_PORT)) //
                .type(targetDir) //
                .build());

        GraphmlEdge edge = context.hyperEdgeBuilder.toEdge();
        graphmlWriter.startEdge(edge);

        elementStack.push(context);
    }

    private void startEndpointElement(Map<String, String> attributes) {
        GraphmlEndpoint.GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder();

        if (attributes != null) {
            String id = attributes.get("id");
            if (id != null) {
                builder.id(id);
            }

            String node = attributes.get("node");
            if (node != null) {
                builder.node(node);
            }

            String port = attributes.get("port");
            if (port != null) {
                builder.port(port);
            }

            String type = attributes.get("type");
            if (type != null) {
                builder.type(GraphmlDirection.getDirection(type));
            }
        }

        ElementContext context = new ElementContext(GraphmlElements.ENDPOINT, attributes, false);
        context.endpoint = builder.build();
        elementStack.push(context);
    }

    private void startGraphElement(Map<String, String> attributes) throws IOException {
        GraphmlGraph.GraphmlGraphBuilder builder = IGraphmlGraph.builder();

        if (attributes != null) {
            String id = attributes.get("id");
            if (id != null) {
                builder.id(id);
            }

            String edgeDefault = attributes.get("edgedefault");
            if (edgeDefault != null) {
                if ("directed".equals(edgeDefault)) {
                    builder.edgedefault(GraphmlGraph.EdgeDefault.directed);
                } else if ("undirected".equals(edgeDefault)) {
                    builder.edgedefault(GraphmlGraph.EdgeDefault.undirected);
                }
            }

            builder.attributes(attributes);
        }

        ElementContext context = new ElementContext(GraphmlElements.GRAPH, attributes, false);
        context.graph = builder.build();
        elementStack.push(context);
        graphmlWriter.startGraph(context.graph);
    }

    private void startGraphmlElement(Map<String, String> attributes) throws IOException {
        GraphmlDocument.GraphmlDocumentBuilder builder = IGraphmlDocument.builder();
        if (attributes != null && !attributes.isEmpty()) {
            builder.attributes(attributes);
        }
        ElementContext context = new ElementContext(GraphmlElements.GRAPHML, attributes, false);
        context.document = builder.build();
        elementStack.push(context);
        graphmlWriter.startDocument(context.document);
    }

    private void startHyperEdgeElement(Map<String, String> attributes) {
        String id = null;
        if (attributes != null) {
            id = attributes.get("id");
        }

        // GraphmlHyperEdge builder requires id in constructor
        GraphmlHyperEdge.GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder(id != null ? id : "");

        ElementContext context = new ElementContext(GraphmlElements.HYPER_EDGE, attributes, false);
        context.hyperEdgeBuilder = builder;
        elementStack.push(context);
        // Don't build the hyperedge yet - wait for endpoints to be added
    }

    private void startKeyElement(Map<String, String> attributes) {
        GraphmlKey.GraphmlKeyBuilder builder = IGraphmlKey.builder();

        if (attributes != null) {
            String id = attributes.get("id");
            if (id != null) {
                builder.id(id);
            }

            String forAttr = attributes.get("for");
            if (forAttr != null) {
                builder.forType(GraphmlKeyForType.keyForType(forAttr));
            }

            String attrName = attributes.get("attr.name");
            if (attrName != null) {
                builder.attrName(attrName);
            }

            String attrType = attributes.get("attr.type");
            if (attrType != null) {
                builder.attrType(attrType);
            }

            builder.attributes(attributes);
        }

        ElementContext context = new ElementContext(GraphmlElements.KEY, attributes, false);
        context.key = builder.build();
        elementStack.push(context);
    }

    private void startLocatorElement(Map<String, String> attributes) {
        ElementContext context = new ElementContext(GraphmlElements.LOCATOR, attributes, false);
        elementStack.push(context);
    }

    private void startNodeElement(Map<String, String> attributes) throws IOException {
        GraphmlNode.GraphmlNodeBuilder builder = GraphmlNode.builder();

        if (attributes != null) {
            String id = attributes.get("id");
            if (id != null) {
                builder.id(id);
            }
            builder.attributes(attributes);
        }

        ElementContext context = new ElementContext(GraphmlElements.NODE, attributes, false);
        context.node = builder.build();
        elementStack.push(context);
        graphmlWriter.startNode(context.node);
    }

    private void startPortElement(Map<String, String> attributes) throws IOException {
        GraphmlPort.GraphmlPortBuilder builder = IGraphmlPort.builder();

        if (attributes != null) {
            String name = attributes.get("name");
            if (name != null) {
                builder.name(name);
            }
            builder.attributes(attributes);
        }

        ElementContext context = new ElementContext(GraphmlElements.PORT, attributes, false);
        context.port = builder.build();
        elementStack.push(context);
        graphmlWriter.startPort(context.port);
    }

}
