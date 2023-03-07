package com.calpano.graphinout.base.graphml;


import com.calpano.graphinout.base.xml.XmlWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI_SCHEMA_LOCATIOM;

@Slf4j
public class GraphmlWriterImpl implements GraphmlWriter {

    final XmlWriter xmlWriter;

    public GraphmlWriterImpl(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void key(GraphmlKey data) throws IOException {
        log.trace("data [{}]", data);
        xmlWriter.startElement(GraphmlKey.TAGNAME, data.getAttributes());
        if (data.getDesc() != null) {
            data.getDesc().writeXml(xmlWriter);
        }
        writerData(data.getDataList());
        if (data.getDefaultValue() != null) {
            xmlWriter.startElement(GraphmlDefault.TAGNAME, data.getDefaultValue().getAttributes());
            xmlWriter.characterData(data.getDefaultValue().getValue());
            xmlWriter.endElement(GraphmlDefault.TAGNAME);
        }
        xmlWriter.endElement(GraphmlKey.TAGNAME);
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
    public void endGraph(Optional<GraphmlLocator> locator) throws IOException {
        log.trace("endGraph [{}]", locator.isPresent() ? locator.get() : null);
        if (locator.isPresent()) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.get().getAttributes());
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
    public void endNode(Optional<GraphmlLocator> locator) throws IOException {
        log.trace("endNode [{}]", locator.isPresent() ? locator.get() : null);
        if (locator.isPresent()) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.get().getAttributes());
            xmlWriter.endElement(GraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(GraphmlNode.TAGNAME);
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

        writerData(doc.getDataList());
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
        writerData(graphmlGraph.getDataList());
        xmlWriter.lineBreak();
    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge edge) throws IOException {
        log.trace("startHyperEdge [{}]", edge);
        xmlWriter.startElement(GraphmlHyperEdge.TAGNAME, edge.getAttributes());
        if (edge.desc != null) {
            edge.desc.writeXml(xmlWriter);
        }
        writerData(edge.getDataList());

        if (edge.getEndpoints() != null) {
            for (GraphmlEndpoint endpoint : edge.getEndpoints()) {
                xmlWriter.startElement(GraphmlEndpoint.TAGNAME, endpoint.getAttributes());
                xmlWriter.endElement(GraphmlEndpoint.TAGNAME);
            }
        }

    }

    // TODO split into startNode and endNode to have the sub-graphs in between
    @Override
    public void startNode(GraphmlNode node) throws IOException {
        log.trace("startNode [{}]", node);
        xmlWriter.startElement(GraphmlNode.TAGNAME, node.getAttributes());
        if (node.desc != null) {
            node.desc.writeXml(xmlWriter);
        }
        writerData(node.dataList);
        if (node.getPorts() != null) for (GraphmlPort port : node.getPorts()) {
            xmlWriter.startElement(GraphmlPort.TAGNAME, port.getAttributes());
            xmlWriter.endElement(GraphmlPort.TAGNAME);
        }
    }

    @Override
    public void data(GraphmlData data) throws IOException {
        // TODO implement
    }

    @Override
    public void startPort(GraphmlPort port) throws IOException {
        // TODO implement
    }

    @Override
    public void endPort() throws IOException {
        // TODO implement
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
