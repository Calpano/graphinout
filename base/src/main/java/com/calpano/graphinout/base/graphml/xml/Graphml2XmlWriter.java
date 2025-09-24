package com.calpano.graphinout.base.graphml.xml;


import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static org.slf4j.LoggerFactory.getLogger;

public class Graphml2XmlWriter implements GraphmlWriter {

    private static final Logger log = getLogger(Graphml2XmlWriter.class);

    final XmlWriter xmlWriter;
    private final Stack<IGraphmlGraph> graphStack = new Stack<>();

    public Graphml2XmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        xmlWriter.lineBreak();
        log.trace("writerData [{}]", data);
        xmlWriter.elementStart(data.tagName(), data.xmlPlusGraphmlAttributesNormalized());

        String dataValue = data.value();
        if (dataValue != null) {
            if (data.isRawXml()) {
                xmlWriter.raw(dataValue);
            } else {
                xmlWriter.characterDataWhichMayContainCdata(dataValue);
            }
        }
        xmlWriter.elementEnd(data.tagName());
    }

    @Override
    public void documentEnd() throws IOException {
        log.trace("endDocument");
        xmlWriter.elementEnd(GraphmlElements.GRAPHML);
        xmlWriter.documentEnd();
    }

    @Override
    public void documentStart(IGraphmlDocument doc) throws IOException {
        assert doc != null;
        log.trace("startDocument [{}]", doc);
        xmlWriter.documentStart();
        xmlWriter.elementStart(GraphmlElements.GRAPHML, doc.xmlPlusGraphmlAttributesNormalized());
        if (doc.desc() != null) {
            doc.desc().writeXml(xmlWriter);
        }
        xmlWriter.lineBreak();
    }

    @Override
    public void edgeEnd() throws IOException {
        xmlWriter.elementEnd(GraphmlElements.EDGE);
    }

    @Override
    public void edgeStart(IGraphmlEdge edge) throws IOException {
        boolean isDirectedInGraph = IGraphmlGraph.EdgeDefault.isDirected(graphStack.peek().edgeDefault());
        xmlWriter.lineBreak();
        Map<String, String> allAttributesNormalized = edge.xmlPlusGraphmlAttributesNormalized();
        if (edge.directed() != null) {
            boolean isDirectedInEdge = Objects.requireNonNull(edge.directed());
            if (isDirectedInGraph == isDirectedInEdge) {
                // attribute not needed
                allAttributesNormalized.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
            }
        }
        xmlWriter.elementStart(GraphmlElements.EDGE, allAttributesNormalized);
    }

    @Override
    public void graphEnd() throws IOException {
        log.trace("endGraph");
        xmlWriter.elementEnd(GraphmlElements.GRAPH);
        assert !graphStack.isEmpty() : "'graph' start missing";
        graphStack.pop();
    }

    @Override
    public void graphStart(IGraphmlGraph graphmlGraph) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startGraph [{}]", graphmlGraph);
        graphStack.push(graphmlGraph);
        xmlWriter.elementStart(GraphmlElements.GRAPH, graphmlGraph.xmlPlusGraphmlAttributesNormalized());
        if (graphmlGraph.desc() != null) {
            graphmlGraph.desc().writeXml(xmlWriter);
        }
        IGraphmlLocator locator = graphmlGraph.locator();
        if (locator != null) {
            xmlWriter.elementStart(GraphmlElements.LOCATOR, locator.xmlPlusGraphmlAttributesNormalized());
            xmlWriter.elementEnd(GraphmlElements.LOCATOR);
        }
    }

    @Override
    public void hyperEdgeEnd() throws IOException {
        xmlWriter.elementEnd(GraphmlElements.HYPER_EDGE);
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException {
        xmlWriter.elementStart(GraphmlElements.HYPER_EDGE, edge.xmlPlusGraphmlAttributesNormalized());
        if (edge.desc() != null) {
            edge.desc().writeXml(xmlWriter);
        }
        if (edge.endpoints() != null) {
            for (IGraphmlEndpoint endpoint : edge.endpoints()) {
                xmlWriter.elementStart(GraphmlElements.ENDPOINT, endpoint.xmlPlusGraphmlAttributesNormalized());
                xmlWriter.elementEnd(GraphmlElements.ENDPOINT);
            }
        }
    }

    @Override
    public void key(IGraphmlKey graphmlKey) throws IOException {
        log.trace("key [{}]", graphmlKey);
        xmlWriter.lineBreak();
        Map<String, String> attributes = graphmlKey.xmlPlusGraphmlAttributesNormalized();
        if (attributes.get(IGraphmlKey.ATTRIBUTE_FOR) != null && attributes.get(IGraphmlKey.ATTRIBUTE_FOR).equals("all"))
            attributes.remove(IGraphmlKey.ATTRIBUTE_FOR);
        xmlWriter.elementStart(GraphmlElements.KEY, attributes);
        if (graphmlKey.desc() != null) {
            graphmlKey.desc().writeXml(xmlWriter);
        }
        IGraphmlDefault defaultValue = graphmlKey.defaultValue();
        if (defaultValue != null) {
            xmlWriter.elementStart(GraphmlElements.DEFAULT, defaultValue.xmlPlusGraphmlAttributesNormalized());
            // FIXME maybe not always correct
            xmlWriter.raw(defaultValue.value());
            xmlWriter.elementEnd(GraphmlElements.DEFAULT);
        }
        xmlWriter.elementEnd(GraphmlElements.KEY);
    }

    @Override
    public void nodeEnd() throws IOException {
        log.trace("endNode");
        xmlWriter.elementEnd("node");
    }

    @Override
    public void nodeStart(IGraphmlNode node) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startNode [{}]", node);
        xmlWriter.elementStart(node.tagName(), node.xmlPlusGraphmlAttributesNormalized());
        if (node.desc() != null) {
            node.desc().writeXml(xmlWriter);
        }
        IGraphmlLocator locator = node.locator();
        if (locator != null) {
            xmlWriter.elementStart(locator.tagName(), locator.xmlPlusGraphmlAttributesNormalized());
            xmlWriter.elementEnd(locator.tagName());
        }
    }

    @Override
    public void portEnd() throws IOException {
        log.debug("endPort .");
        xmlWriter.elementEnd(GraphmlElements.PORT);
    }

    @Override
    public void portStart(IGraphmlPort port) throws IOException {
        log.debug("startPort [{}]", port);
        xmlWriter.elementStart(GraphmlElements.PORT, port.xmlPlusGraphmlAttributesNormalized());
    }

}
