package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.calpano.graphinout.base.graphml.IGraphmlDocument.HEADER_XMLNS;
import static com.calpano.graphinout.base.graphml.IGraphmlDocument.HEADER_XMLNS_XSI;
import static com.calpano.graphinout.base.graphml.IGraphmlDocument.HEADER_XMLNS_XSI_SCHEMA_LOCATIOM;
import static org.slf4j.LoggerFactory.getLogger;

public class Graphml2XmlWriter implements GraphmlWriter {

    private static final Logger log = getLogger(Graphml2XmlWriter.class);

    final XmlWriter xmlWriter;

    public Graphml2XmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void data(IGraphmlData data) throws IOException {
        xmlWriter.lineBreak();
        log.trace("writerData [{}]", data);
        xmlWriter.startElement(data.tagName(), data.attributes());

        String dataValue = data.value();
        if (dataValue != null) {
            if (data.isRawXml()) {
                xmlWriter.raw(dataValue);
            } else {
                xmlWriter.characterDataWhichMayContainCdata(dataValue);
            }
        }
        xmlWriter.endElement(data.tagName());
    }

    @Override
    public void endDocument() throws IOException {
        log.trace("endDocument");
        xmlWriter.endElement(IGraphmlDocument.TAGNAME);
        xmlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        xmlWriter.endElement(GraphmlEdge.TAGNAME);
    }

    @Override
    public void endGraph(@Nullable IGraphmlLocator locator) throws IOException {
        log.trace("endGraph [{}]", locator);
        if (locator != null) {
            xmlWriter.startElement(IGraphmlLocator.TAGNAME, locator.attributes());
            xmlWriter.endElement(IGraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(IGraphmlGraph.TAGNAME);
        assert  !graphStack.isEmpty();
        graphStack.pop();
    }

    @Override
    public void endHyperEdge() throws IOException {
        xmlWriter.endElement(IGraphmlHyperEdge.TAGNAME);
    }

    @Override
    public void endNode(@Nullable IGraphmlLocator locator) throws IOException {
        log.trace("endNode [{}]", locator);
        if (locator != null) {
            xmlWriter.startElement(locator.tagName(), locator.attributes());
            xmlWriter.endElement(locator.tagName());
        }
        xmlWriter.endElement("node");
    }

    @Override
    public void endPort() throws IOException {
        log.debug("endPort .");
        xmlWriter.endElement(IGraphmlPort.TAGNAME);
    }

    @Override
    public void key(IGraphmlKey graphmlKey) throws IOException {
        log.trace("key [{}]", graphmlKey);
        xmlWriter.lineBreak();
        xmlWriter.startElement(IGraphmlKey.TAGNAME, graphmlKey.attributes());
        if (graphmlKey.desc() != null) {
            graphmlKey.desc().writeXml(xmlWriter);
        }
        if (graphmlKey.defaultValue() != null) {
            xmlWriter.startElement(GraphmlDefault.TAGNAME, graphmlKey.defaultValue().attributes());
            xmlWriter.characterData(graphmlKey.defaultValue().value(), false);
            xmlWriter.endElement(GraphmlDefault.TAGNAME);
        }
        xmlWriter.endElement(IGraphmlKey.TAGNAME);
    }

    @Override
    public void startDocument(IGraphmlDocument doc) throws IOException {
        log.trace("startDocument [{}]", doc);
        xmlWriter.startDocument();
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xmlns", HEADER_XMLNS);
        attributes.put("xmlns:xsi", HEADER_XMLNS_XSI);
        attributes.put("xsi:schemaLocation", HEADER_XMLNS_XSI_SCHEMA_LOCATIOM);
        Map<String, String> extraAttrib = doc.customXmlAttributes();
        if (extraAttrib != null) {
            attributes.putAll(extraAttrib);
        }
        xmlWriter.startElement(IGraphmlDocument.TAGNAME, attributes);

        if (doc.desc() != null) {
            doc.desc().writeXml(xmlWriter);
        }

        if (doc.keys() != null) {
            for (IGraphmlKey key : doc.keys()) {
                key(key);
            }
        }
        xmlWriter.lineBreak();
    }

    @Override
    public void startEdge(IGraphmlEdge edge) throws IOException {
        boolean isDirectedInGraph = graphStack.peek().isDirectedEdges();
        xmlWriter.lineBreak();
        Map<String, String> atts = edge.attributes();
        if(edge.directed() !=null) {
            boolean isDirectedInEdge = Objects.requireNonNull(edge.directed());
            if (isDirectedInGraph == isDirectedInEdge) {
                // attribute not needed
                atts.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
            }
        }
        xmlWriter.startElement(GraphmlEdge.TAGNAME, atts);
    }
    private final Stack<IGraphmlGraph> graphStack = new Stack<>();

    @Override
    public void startGraph(IGraphmlGraph graphmlGraph) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startGraph [{}]", graphmlGraph);
        graphStack.push(graphmlGraph);
        xmlWriter.startElement(IGraphmlGraph.TAGNAME, graphmlGraph.attributes());
        if (graphmlGraph.desc() != null) {
            graphmlGraph.desc().writeXml(xmlWriter);
        }
    }

    @Override
    public void startHyperEdge(IGraphmlHyperEdge edge) throws IOException {
        xmlWriter.startElement(IGraphmlHyperEdge.TAGNAME, edge.attributes());
        if (edge.desc() != null) {
            edge.desc().writeXml(xmlWriter);
        }
        if (edge.endpoints() != null) {
            for (IGraphmlEndpoint endpoint : edge.endpoints()) {
                xmlWriter.startElement(IGraphmlEndpoint.TAGNAME, endpoint.attributes());
                xmlWriter.endElement(IGraphmlEndpoint.TAGNAME);
            }
        }
    }

    @Override
    public void startNode(IGraphmlNode node) throws IOException {
        xmlWriter.lineBreak();
        log.trace("startNode [{}]", node);
        xmlWriter.startElement(node.tagName(), node.attributes());
        if (node.desc() != null) {
            node.desc().writeXml(xmlWriter);
        }
    }

    @Override
    public void startPort(IGraphmlPort port) throws IOException {
        log.debug("startPort [{}]", port);
        xmlWriter.startElement(IGraphmlPort.TAGNAME, port.attributes());
    }

}
