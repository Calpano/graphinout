package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlElement;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDefaultBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDescriptionBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlLocatorBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.builder.ILocatorBuilder;
import com.calpano.graphinout.foundation.xml.IXmlName;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/** Helper class to track element context */
public class XmlElementContext {

    final IXmlName xmlElementName;
    final Map<String, String> xmlAttributes;
    /** is this element representing a raw XML element? */
    final boolean isRawXml;
    /** will produce an {@link IGraphmlElement} or subtype thereof */
    private final GraphmlElementBuilder<?> builder;
    private final @Nullable XmlElementContext parent;
    private boolean isStarted;

    /**
     * @param xmlElementName aka tagName
     * @param xmlAttributes  all XML attributes
     * @param isRawXml    is this any raw XML element from inside a data element? If false, this is a GraphML element
     */
    XmlElementContext(@Nullable XmlElementContext parent, IXmlName xmlElementName, Map<String, String> xmlAttributes, boolean isRawXml, GraphmlElementBuilder<?> builder) {
        this.parent = parent;
        this.xmlElementName = xmlElementName;
        this.xmlAttributes = xmlAttributes;
        this.isRawXml = isRawXml;
        this.builder = builder;
        this.isStarted = false;
    }

    public ILocatorBuilder builderWithLocatorSupport() {
        ILocatorBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDataBuilder dataBuilder() {
        GraphmlDataBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDefaultBuilder defaultBuilder() {
        GraphmlDefaultBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDescriptionBuilder descBuilder() {
        GraphmlDescriptionBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDocumentBuilder documentBuilder() {
        GraphmlDocumentBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlEndpointBuilder endpointBuilder() {
        GraphmlEndpointBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlGraphBuilder graphBuilder() {
        GraphmlGraphBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlHyperEdgeBuilder hyperEdgeBuilder() {
        GraphmlHyperEdgeBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public GraphmlKeyBuilder keyBuilder() {
        GraphmlKeyBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlLocatorBuilder locatorBuilder() {
        GraphmlLocatorBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    /**
     * Mark we wrote the START of this element to the downstream {@link GraphmlWriter}.
     */
    public void markAsStarted() {
        isStarted = true;
    }

    public void maybeWriteStartTo(GraphmlWriter graphmlWriter) throws IOException {
        if (isStarted())
            return;
        if (parent != null) {
            parent.maybeWriteStartTo(graphmlWriter);
        }
        switch (xmlElementName.localName()) {
            case GraphmlElements.DATA -> graphmlWriter.data(dataBuilder().build());
            case GraphmlElements.GRAPHML -> graphmlWriter.documentStart(documentBuilder().build());
            case GraphmlElements.GRAPH -> graphmlWriter.graphStart(graphBuilder().build());
            case GraphmlElements.NODE -> graphmlWriter.nodeStart(nodeBuilder().build());
            case GraphmlElements.PORT -> graphmlWriter.portStart(portBuilder().build());
            case GraphmlElements.HYPER_EDGE -> graphmlWriter.hyperEdgeStart(hyperEdgeBuilder().build());
            case GraphmlElements.KEY -> graphmlWriter.key(keyBuilder().build());
            case GraphmlElements.EDGE -> graphmlWriter.edgeStart(hyperEdgeBuilder().toEdge());
            default -> throw new IllegalArgumentException("Cannot start " + xmlElementName);
        }
        markAsStarted();
    }

    public GraphmlNodeBuilder nodeBuilder() {
        GraphmlNodeBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlPortBuilder portBuilder() {
        GraphmlPortBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public void writeEndTo(GraphmlWriter graphmlWriter) throws IOException {
        maybeWriteStartTo(graphmlWriter);
        switch (xmlElementName.localName()) {
            case GraphmlElements.GRAPHML -> graphmlWriter.documentEnd();
            case GraphmlElements.GRAPH -> graphmlWriter.graphEnd();
            case GraphmlElements.NODE -> graphmlWriter.nodeEnd();
            case GraphmlElements.PORT -> graphmlWriter.portEnd();
            case GraphmlElements.HYPER_EDGE -> graphmlWriter.hyperEdgeEnd();
            case GraphmlElements.EDGE -> graphmlWriter.edgeEnd();
            default -> throw new IllegalArgumentException("Cannot end " + xmlElementName);
        }
        markAsStarted();
    }

    private <T extends GraphmlElementBuilder<T>> T builder() {
        //noinspection unchecked
        return (T) builder;
    }

}
