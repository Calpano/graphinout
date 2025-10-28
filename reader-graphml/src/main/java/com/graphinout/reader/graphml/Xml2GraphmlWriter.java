package com.graphinout.reader.graphml;

import com.graphinout.reader.graphml.elements.GraphmlDataType;
import com.graphinout.reader.graphml.elements.GraphmlDirection;
import com.graphinout.reader.graphml.elements.GraphmlElements;
import com.graphinout.reader.graphml.elements.GraphmlKeyForType;
import com.graphinout.reader.graphml.elements.IGraphmlData;
import com.graphinout.reader.graphml.elements.IGraphmlDefault;
import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlDocument;
import com.graphinout.reader.graphml.elements.IGraphmlEdge;
import com.graphinout.reader.graphml.elements.IGraphmlEndpoint;
import com.graphinout.reader.graphml.elements.IGraphmlGraph;
import com.graphinout.reader.graphml.elements.IGraphmlHyperEdge;
import com.graphinout.reader.graphml.elements.IGraphmlKey;
import com.graphinout.reader.graphml.elements.IGraphmlLocator;
import com.graphinout.reader.graphml.elements.IGraphmlNode;
import com.graphinout.reader.graphml.elements.IGraphmlPort;
import com.graphinout.reader.graphml.elements.builder.GraphmlDataBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlDefaultBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlDocumentBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlElementBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlEndpointBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlGraphBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlHyperEdgeBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlKeyBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlLocatorBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlNodeBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlPortBuilder;
import com.graphinout.reader.graphml.elements.impl.GraphmlDescription;
import com.graphinout.reader.graphml.elements.impl.GraphmlEndpoint;
import com.graphinout.base.reader.Location;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.ContentErrorException;
import com.graphinout.foundation.xml.BaseXmlHandler;
import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.IXmlName;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XML.XmlSpace;
import com.graphinout.foundation.xml.Xml2DocumentFragmentWriter;
import com.graphinout.foundation.xml.XmlFragmentString;
import com.graphinout.foundation.xml.XmlWriter;
import com.graphinout.foundation.xml.element.XmlDocumentFragment;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.graphinout.reader.graphml.elements.GraphmlElements.DATA;
import static com.graphinout.reader.graphml.elements.GraphmlElements.DEFAULT;
import static com.graphinout.reader.graphml.elements.GraphmlElements.DESC;
import static com.graphinout.reader.graphml.elements.GraphmlElements.EDGE;
import static com.graphinout.reader.graphml.elements.GraphmlElements.ENDPOINT;
import static com.graphinout.reader.graphml.elements.GraphmlElements.GRAPH;
import static com.graphinout.reader.graphml.elements.GraphmlElements.GRAPHML;
import static com.graphinout.reader.graphml.elements.GraphmlElements.HYPER_EDGE;
import static com.graphinout.reader.graphml.elements.GraphmlElements.KEY;
import static com.graphinout.reader.graphml.elements.GraphmlElements.LOCATOR;
import static com.graphinout.reader.graphml.elements.GraphmlElements.NODE;
import static com.graphinout.reader.graphml.elements.GraphmlElements.PORT;
import static com.graphinout.reader.graphml.elements.IGraphmlData.ATTRIBUTE_KEY;
import static com.graphinout.reader.graphml.elements.IGraphmlElementWithId.ATTRIBUTE_ID;
import static com.graphinout.reader.graphml.elements.IGraphmlEndpoint.ATTRIBUTE_NODE;
import static com.graphinout.reader.graphml.elements.IGraphmlEndpoint.ATTRIBUTE_PORT;
import static com.graphinout.reader.graphml.elements.IGraphmlEndpoint.ATTRIBUTE_TYPE;
import static com.graphinout.reader.graphml.elements.IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT;
import static com.graphinout.reader.graphml.elements.IGraphmlPort.ATTRIBUTE_NAME;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.graphinout.foundation.util.Nullables.mapOrNull;
import static com.graphinout.foundation.xml.XmlTool.ifAttributeNotNull;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Interprets incoming XML calls. Buffers them. Emits GraphML calls to downstream.
 * <p>
 * Concerns: Parsing GraphML XML vs. generic XML (as it occurs in KEY-DEFAULT or DATA elements). This is handled via
 * {@link XmlMode} and {@link XmlElementContext#isRawXml}.
 */
public class Xml2GraphmlWriter extends BaseXmlHandler implements XmlWriter {

    private static final Logger log = getLogger(Xml2GraphmlWriter.class);
    private final Map<String, IGraphmlKey> dataId_key = new HashMap<>();
    /** Buffers XML content for {#code <key><default>}, {@code <data>} and {#code <desc>} elements. */
    private final Xml2DocumentFragmentWriter xmlBuffer = new Xml2DocumentFragmentWriter();
    /** downstream writer */
    private final GraphmlWriter graphmlWriter;
    /** also required to detect the end of raw XML */
    private final XmlParseContext elementStack = new XmlParseContext();
    /** XML is buffered during 'PCDATA' as in {@code <key><default>, <data>, and <desc>} elements. */
    private boolean isBufferingXml = false;

    /**
     * @param graphmlWriter downstream
     */
    public Xml2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    public void characters(String characters, CharactersKind kind) throws ContentErrorException {
        if (isBufferingXml) {
            // Accumulate character data for elements that need it (like data, desc, default)
            xmlBuffer.characters(characters, kind);
        } else {
            if (!characters.trim().isEmpty()) {
                // characters between elements when not in data/desc/default -> not allowed in Graphml
                throw sendContentError_Error("Unexpected content ('" + characters +
                        "') outside Graphml content tags.");
            }
        }
    }

    public void charactersEnd() {
        if (isBufferingXml) {
            xmlBuffer.charactersEnd();
        } else {
            assert elementStack.isInterpretedAsGraphml();
        }
    }

    public void charactersStart() {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            xmlBuffer.charactersStart();
        }
    }

    @Override
    public void documentEnd() {
        // we write document end on </graphml>
    }

    @Override
    public void documentStart() {
        // Document will be started when we encounter the graphml element
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        // even if we are buffering xml, we need to inspect this
        IXmlName xmlName = IXmlName.of(uri, localName, qName);

        if (isBufferingXml && !xmlName.localName().equals(DATA) && !xmlName.localName().equals(DESC) && !xmlName.localName().equals(DEFAULT)) {
            // keep on buffering
            xmlBuffer.elementEnd(xmlName);
            // remove corresponding context, which we must keep to identify the end of raw xml
            elementStack.pop(localName);
            // IMPROVE remove this check???
            XmlElementContext parent = elementStack.peekNullable();
            if (parent == null) {
                throw new IllegalStateException("No parent for element: " + localName);
            }
            return;
        }

        switch (localName) {
            case GRAPHML -> graphmlDocumentEnd();
            case GRAPH -> graphmlGraphEnd();
            case NODE -> graphmlNodeEnd();
            case EDGE -> graphmlEdgeEnd();
            case HYPER_EDGE -> graphmlHyperedgeEnd();
            case PORT -> graphmlPortEnd();
            case KEY -> graphmlKeyEnd();
            case DATA -> graphmlDataEnd();
            case DESC -> graphmlDescEnd();
            case DEFAULT -> graphmlDefaultEnd();
            case LOCATOR -> graphmlLocatorEnd();
            case ENDPOINT -> graphmlEndpointEnd();
            default -> throw sendContentError_Error("The Element </" + localName + "> not acceptable tag for Graphml.");
        }
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            // push to stack and emit to rawXmlBuffer
            IXmlName xmlName = IXmlName.of(uri, localName, qName);
            elementStack.push(xmlName, attributes, true, null, XmlMode.GENERIC_PC_DATA);
            xmlBuffer.elementStart(xmlName, attributes);
            return;
        }

        switch (localName) {
            case GRAPHML -> graphmlDocumentStart(attributes);
            case GRAPH -> graphmlGraphStart(attributes);
            case NODE -> graphmlNodeStart(attributes);
            case EDGE -> graphmlEdgeStart(attributes);
            case HYPER_EDGE -> graphmlHyperedgeStart(attributes);
            case PORT -> graphmlPortStart(attributes);
            case KEY -> graphmlKeyStart(attributes);
            case DATA -> graphmlDataStart(attributes);
            case DESC -> graphmlDescStart(attributes);
            case DEFAULT -> graphmlDefaultStart(attributes);
            case LOCATOR -> graphmlLocatorStart(attributes);
            case ENDPOINT -> graphmlEndpointStart(attributes);
            default -> throw sendContentError_Error("XML Element <" + localName + "> is not a Graphml tag and not allowing XML here. "+stackToString());
        }
    }

    private String stackToString() {
        return elementStack.toString();
    }

    @Override
    public void lineBreak() throws IOException {
        if (isBufferingXml) {
            xmlBuffer.lineBreak();
        }
        // Line breaks are typically ignored in GraphML processing
    }

    @Override
    public void raw(String rawXml) {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            xmlBuffer.raw(rawXml);
        } else {
            throw new UnsupportedOperationException("no raw XML in GraphML");
        }
    }

    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        super.setContentErrorHandler(errorHandler);
        // chain
        graphmlWriter.setContentErrorHandler(errorHandler);
    }

    private @Nullable XmlFragmentString bufferedXml(XmlSpace xmlSpace) {
        return mapOrNull(xmlBuffer.resultFragment(xmlSpace), XmlDocumentFragment::toXmlFragmentString);
    }

    private void bufferingXmlEnd() {
        this.isBufferingXml = false;
        xmlBuffer.fragmentEnd();
    }

    private void bufferingXmlStart() {
        this.isBufferingXml = true;
        xmlBuffer.fragmentStart();
    }

    private @Nullable RuntimeException buildException(ContentError.ErrorLevel errorLevel, Exception e) {
        Location location = Locator.locationOrNotAvailable(locator());
        ContentError contentError = new ContentError(errorLevel, e.getMessage(), location);
        onContentError(contentError);

        if (errorLevel == ContentError.ErrorLevel.Error) {
            return new RuntimeException("While parsing " + location + "\n" + "Stack: " + elementStack + "\n" + "Message: " + e.getMessage(), e);
        } else {
            log.warn("ContentError: " + contentError, e);
            return null;
        }
    }

    /**
     * Characters already in builder. Emitted stand-alone.
     */
    private void graphmlDataEnd() throws IOException {
        bufferingXmlEnd();
        XmlElementContext dataContext = elementStack.pop(GraphmlElements.DATA);
        XmlElementContext parent = elementStack.peek_(GRAPHML, GRAPH, NODE, EDGE, HYPER_EDGE, PORT);
        parent.maybeWriteStartTo(graphmlWriter);

        // apply xmlSpace from surrounding element
        XmlSpace xmlSpace = XmlSpace.fromAttributesValue(dataContext.xmlAttributes.get(XML.XML_SPACE));
        // do we need to report anything in <data> ?
        XmlFragmentString xmlFragmentString = bufferedXml(XmlSpace.default_);
        if (xmlSpace == XmlSpace.preserve || xmlFragmentString != null) {
            GraphmlDataBuilder builder = dataContext.dataBuilder();
            builder.xmlValue(xmlFragmentString);
            if (xmlSpace == XmlSpace.preserve) {
                builder.attribute(XML.XML_SPACE, xmlSpace.xmlAttValue);
            }
            IGraphmlData data = builder.build();
            try {
                graphmlWriter.data(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void graphmlDataStart(Map<String, String> attributes) {
        GraphmlDataBuilder builder = IGraphmlData.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_KEY, builder::key);

        elementStack.push(Graphml.xmlNameOf(GraphmlElements.DATA), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void graphmlDefaultEnd() {
        bufferingXmlEnd();
        elementStack.pop(DEFAULT);

        GraphmlKeyBuilder keyBuilder = elementStack.peek_().keyBuilder();
        ifPresentAccept(bufferedXml(XmlSpace.default_), xmlFragmentString -> {
            IGraphmlDefault graphmlDefault = IGraphmlDefault.of(xmlFragmentString);
            keyBuilder.defaultValue(graphmlDefault);
        });

        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void graphmlDefaultStart(Map<String, String> attributes) {
        GraphmlDefaultBuilder builder = IGraphmlDefault.builder();
        builder.attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(DEFAULT), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void graphmlDescEnd() throws IOException {
        bufferingXmlEnd();
        XmlElementContext descContext = elementStack.pop(DESC);
        // Set description for the parent element
        XmlElementContext parent = elementStack.peek_();

        ifPresentAccept(bufferedXml(XmlSpace.default_), xmlFragmentString -> {
            GraphmlDescription desc = descContext.descBuilder().xmlValue(xmlFragmentString).build();
            switch (parent.xmlElementName.localName()) {
                case GRAPHML -> parent.documentBuilder().desc(desc);
                case GRAPH -> parent.graphBuilder().desc(desc);
                case NODE -> parent.nodeBuilder().desc(desc);
                case EDGE, HYPER_EDGE -> parent.hyperEdgeBuilder().desc(desc);
                case KEY -> parent.keyBuilder().desc(desc);
            }
        });

        parent.maybeWriteStartTo(graphmlWriter);
        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void graphmlDescStart(Map<String, String> attributes) {
        GraphmlElementBuilder<?> builder = IGraphmlDescription.builder().attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(DESC), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void graphmlDocumentEnd() throws IOException {
        elementStack.pop(GRAPHML).writeEndTo(graphmlWriter);
    }

    private void graphmlDocumentStart(Map<String, String> attributes) {
        GraphmlDocumentBuilder builder = IGraphmlDocument.builder();
        builder.attributes(attributes);

        elementStack.push(Graphml.xmlNameOf(GRAPHML), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes FIXME or <key>
    }

    private void graphmlEdgeEnd() throws IOException {
        XmlElementContext context = elementStack.pop(EDGE);
        context.writeEndTo(graphmlWriter);
    }

    private void graphmlEdgeStart(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        // init from containing graph
        GraphmlGraphBuilder parentGraphElement = elementStack.findParentGraphElement();
        assert parentGraphElement != null : "Found no active parent graph element";
        IGraphmlGraph.EdgeDefault edgeDefault = requireNonNull(parentGraphElement).edgeDefault();
        boolean isDirected = IGraphmlGraph.EdgeDefault.isDirected(edgeDefault);

        // maybe overwrite from edge attributes
        @Nullable String b = attributes.get(IGraphmlEdge.ATTRIBUTE_DIRECTED);
        if (b != null) {
            isDirected = Boolean.parseBoolean(b);
        }

        GraphmlDirection sourceDir = isDirected ? GraphmlDirection.In : GraphmlDirection.Undirected;
        builder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE_PORT)) //
                .type(sourceDir) //
                .build());
        GraphmlDirection targetDir = isDirected ? GraphmlDirection.Out : GraphmlDirection.Undirected;
        builder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET_PORT)) //
                .type(targetDir) //
                .build());

        elementStack.push(Graphml.xmlNameOf(EDGE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void graphmlEndpointEnd() {
        XmlElementContext context = elementStack.pop(GraphmlElements.ENDPOINT);
        // Add the endpoint to the parent hyperedge builder
        if (!elementStack.isEmpty() && context.endpointBuilder() != null) {
            XmlElementContext parentContext = elementStack.peek_();
            assert parentContext.hyperEdgeBuilder() != null;
            GraphmlEndpoint graphmlEndpoint = context.endpointBuilder().build();
            parentContext.hyperEdgeBuilder().addEndpoint(graphmlEndpoint);
        }
        // TODO else: parse warning
    }

    private void graphmlEndpointStart(Map<String, String> attributes) {
        GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_NODE, builder::node);
        ifAttributeNotNull(attributes, ATTRIBUTE_PORT, builder::port);
        ifAttributeNotNull(attributes, ATTRIBUTE_TYPE, value -> builder.type(GraphmlDirection.getDirection(value)));

        elementStack.push(Graphml.xmlNameOf(ENDPOINT), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void graphmlGraphEnd() throws IOException {
        elementStack.pop(GRAPH).writeEndTo(graphmlWriter);
    }

    private void graphmlGraphStart(Map<String, String> attributes) throws IOException {
        // elementStack.peek_(GRAPH, GRAPHML).maybeWriteStartTo(graphmlWriter);

        GraphmlGraphBuilder builder = IGraphmlGraph.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_EDGE_DEFAULT, value -> //
                builder.edgeDefault(IGraphmlGraph.EdgeDefault.valueOf(value)));

        elementStack.push(Graphml.xmlNameOf(GRAPH), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <key> comes
    }

    private void graphmlHyperedgeEnd() throws IOException {
        XmlElementContext context = elementStack.pop(GraphmlElements.HYPER_EDGE);
        context.writeEndTo(graphmlWriter);
    }

    private void graphmlHyperedgeStart(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.push(Graphml.xmlNameOf(HYPER_EDGE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void graphmlKeyEnd() throws IOException {
        XmlElementContext context = elementStack.pop(GraphmlElements.KEY);
        XmlElementContext parentContext = elementStack.peek_();
        parentContext.maybeWriteStartTo(graphmlWriter);

        IGraphmlKey key = context.keyBuilder().build();
        context.maybeWriteStartTo(graphmlWriter);

        String id = key.id();
        // Graphml ID semantics state the id must be unique
        IGraphmlKey prev = dataId_key.put(id, key);
        if(prev != null) {
            throw sendContentError_Error("<key id> used multiple times for different keys. Check "+prev+" and "+key);
        }
    }

    private void graphmlKeyStart(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_FOR, value -> builder.forType(GraphmlKeyForType.keyForType(value)));
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_NAME, builder::attrName);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_TYPE, str -> builder.attrType(GraphmlDataType.fromGraphmlName(str)));
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, id -> {
            builder.id(id);
            IGraphmlKey prev = dataId_key.get(id);
            if(prev!=null) {
                throw sendContentError_Error("<key id> used multiple times for different keys. Check "+prev+" and "+builder.build());
            }
        });

        elementStack.push(Graphml.xmlNameOf(KEY), attributes, false, builder, XmlMode.Graphml);
        // don't build <key> yet, maybe <default> comes
    }

    private void graphmlLocatorEnd() {
        GraphmlLocatorBuilder locatorBuilder = elementStack.pop(GraphmlElements.LOCATOR).locatorBuilder();
        IGraphmlLocator locator = locatorBuilder.build();
        elementStack.peek_().builderWithLocatorSupport().locator(locator);
    }

    private void graphmlLocatorStart(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlLocatorBuilder builder = IGraphmlLocator.builder();
        builder.attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(LOCATOR), attributes, false, builder, XmlMode.Graphml);
        // element is empty, but we wait for end
    }

    private void graphmlNodeEnd() throws IOException {
        XmlElementContext context = elementStack.pop(NODE);
        context.writeEndTo(graphmlWriter);
    }

    private void graphmlNodeStart(Map<String, String> attributes) throws IOException {
        GraphmlNodeBuilder builder = IGraphmlNode.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        elementStack.push(Graphml.xmlNameOf(NODE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void graphmlPortEnd() throws IOException {
        XmlElementContext context = elementStack.pop(PORT);
        context.writeEndTo(graphmlWriter);
    }

    private void graphmlPortStart(Map<String, String> attributes) throws IOException {
        GraphmlPortBuilder builder = IGraphmlPort.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_NAME, builder::name);

        XmlElementContext context = elementStack.push(Graphml.xmlNameOf(GraphmlElements.PORT), attributes, false, builder, XmlMode.Graphml);
        context.maybeWriteStartTo(graphmlWriter);
    }

    // TODO use in elementStart / end to switch buffering xml
    private boolean isContentElement() {
        XmlElementContext context = elementStack.peekNullable();
        if (context == null) return false;
        return switch (context.xmlElementName.localName()) {
            case DATA, DESC, DEFAULT -> true;
            default -> false;
        };
    }

}
