package com.calpano.graphinout.base.graphml;


import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI_SCHEMA_LOCATIOM;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlWriterImpl implements GraphmlWriter {

    private static final Logger log = getLogger(GraphmlWriterImpl.class);

    final XmlWriter xmlWriter;

    public GraphmlWriterImpl(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void data(GraphmlData data) throws IOException {
        writerData(data);
    }

    @Override
    public void endDocument() throws IOException {
        log.trace("endDocument");
        xmlWriter.endElement(GraphmlDocument.TAGNAME);
        xmlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        xmlWriter.endElement(GraphmlEdge.TAGNAME);
        xmlWriter.lineBreak();
    }

    @Override
    public void endGraph(@Nullable GraphmlLocator locator) throws IOException {
        log.trace("endGraph [{}]", locator);
        if (locator != null) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.getAttributes());
            xmlWriter.endElement(GraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(GraphmlGraph.TAGNAME);
        xmlWriter.lineBreak();
    }

    @Override
    public void endHyperEdge() throws IOException {
        xmlWriter.endElement(GraphmlHyperEdge.TAGNAME);
        xmlWriter.lineBreak();
    }

    @Override
    public void endNode(@Nullable GraphmlLocator locator) throws IOException {
        log.trace("endNode [{}]", locator);
        if (locator != null) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.getAttributes());
            xmlWriter.endElement(GraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(GraphmlNode.TAGNAME);
        xmlWriter.lineBreak();
    }

    @Override
    public void endPort() throws IOException {
        log.debug("endPort .");
        xmlWriter.endElement(GraphmlPort.TAGNAME);
    }

    @Override
    public void key(GraphmlKey graphmlKey) throws IOException {
        log.trace("key [{}]", graphmlKey);
        xmlWriter.startElement(GraphmlKey.TAGNAME, graphmlKey.getAttributes());
        if (graphmlKey.getDesc() != null) {
            graphmlKey.getDesc().writeXml(xmlWriter);
        }
        if (graphmlKey.getDefaultValue() != null) {
            xmlWriter.startElement(GraphmlDefault.TAGNAME, graphmlKey.getDefaultValue().getAttributes());
            xmlWriter.characterData(graphmlKey.getDefaultValue().getValue());
            xmlWriter.endElement(GraphmlDefault.TAGNAME);
        }
        xmlWriter.endElement(GraphmlKey.TAGNAME);
        xmlWriter.lineBreak();
    }

    @Override
    public void startDocument(GraphmlDocument doc) throws IOException {
        log.trace("startDocument [{}]", doc);
        xmlWriter.startDocument();
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xmlns", HEADER_XMLNS);
        attributes.put("xmlns:xsi", HEADER_XMLNS_XSI);
        attributes.put("xsi:schemaLocation", HEADER_XMLNS_XSI_SCHEMA_LOCATIOM);
        Map<String, String> extraAttrib = doc.getExtraAttrib();
        if (extraAttrib != null) {
            extraAttrib.forEach((k, v) -> attributes.put(k, v));
        }
        xmlWriter.startElement(GraphmlDocument.TAGNAME, attributes);
        xmlWriter.lineBreak();

        if (doc.desc != null) {
            doc.desc.writeXml(xmlWriter);
        }

        if (doc.getKeys() != null) {
            for (GraphmlKey key : doc.getKeys()) {
                key(key);
            }
        }

        xmlWriter.lineBreak();
    }

    @Override
    public void startEdge(GraphmlEdge edge) throws IOException {
        xmlWriter.startElement(GraphmlEdge.TAGNAME, edge.getAttributes());
    }

    @Override
    public void startGraph(GraphmlGraph graphmlGraph) throws IOException {
        log.trace("startGraph [{}]", graphmlGraph);
        xmlWriter.startElement(GraphmlGraph.TAGNAME, graphmlGraph.getAttributes());
        if (graphmlGraph.desc != null) {
            graphmlGraph.desc.writeXml(xmlWriter);
        }
        xmlWriter.lineBreak();
    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge edge) throws IOException {
        log.trace("startHyperEdge [{}]", edge);
        xmlWriter.startElement(GraphmlHyperEdge.TAGNAME, edge.getAttributes());
        if (edge.desc != null) {
            edge.desc.writeXml(xmlWriter);
        }
        if (edge.getEndpoints() != null) {
            for (GraphmlEndpoint endpoint : edge.getEndpoints()) {
                xmlWriter.startElement(GraphmlEndpoint.TAGNAME, endpoint.getAttributes());
                xmlWriter.endElement(GraphmlEndpoint.TAGNAME);
            }
        }
    }

    @Override
    public void startNode(GraphmlNode node) throws IOException {
        log.trace("startNode [{}]", node);
        xmlWriter.startElement(GraphmlNode.TAGNAME, node.getAttributes());
        if (node.desc != null) {
            node.desc.writeXml(xmlWriter);
        }
    }

    @Override
    public void startPort(GraphmlPort port) throws IOException {
        log.debug("startPort [{}]", port);
        xmlWriter.startElement(GraphmlPort.TAGNAME, port.getAttributes());
    }

    private void writerData(GraphmlData data) throws IOException {
        log.trace("writerData [{}]", data);
        xmlWriter.startElement(GraphmlData.TAGNAME, data.getAttributes());
        if (data != null && data.getValue() != null) xmlWriter.characterData(data.getValue());
        xmlWriter.endElement(GraphmlData.TAGNAME);
    }

    private void writerData(List<GraphmlData> datas) throws IOException {
        if (datas != null) for (GraphmlData data : datas) {
            writerData(data);
        }
    }

}
