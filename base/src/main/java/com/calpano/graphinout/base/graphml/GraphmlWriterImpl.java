package com.calpano.graphinout.base.graphml;


import com.calpano.graphinout.base.output.xml.XmlWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI;
import static com.calpano.graphinout.base.graphml.GraphmlDocument.HEADER_XMLNS_XSI_SCHEMA_LOCATIOM;

public class GraphmlWriterImpl implements GraphmlWriter {

    final XmlWriter xmlWriter;

    public GraphmlWriterImpl(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void data(GraphmlKey data) throws IOException {
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
        xmlWriter.endElement(GraphmlDocument.TAGNAME);
        xmlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        // TODO implement
        xmlWriter.endElement(GraphmlEdge.TAGNAME);
    }

    @Override
    public void endGraph(Optional<GraphmlLocator> locator) throws IOException {
        if (locator.isPresent()) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.get().getAttributes());
            xmlWriter.endElement(GraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(GraphmlGraph.TAGNAME);
    }

    @Override
    public void endHyperEdge() throws IOException {
        xmlWriter.endElement(GraphmlHyperEdge.TAGNAME);
    }

    @Override
    public void endNode(Optional<GraphmlLocator> locator) throws IOException {
        if (locator.isPresent()) {
            xmlWriter.startElement(GraphmlLocator.TAGNAME, locator.get().getAttributes());
            xmlWriter.endElement(GraphmlLocator.TAGNAME);
        }
        xmlWriter.endElement(GraphmlNode.TAGNAME);
    }

    @Override
    public void startDocument(GraphmlDocument doc) throws IOException {
        xmlWriter.startDocument();
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xmlns", HEADER_XMLNS);
        attributes.put("xmlns:xsi", HEADER_XMLNS_XSI);
        attributes.put("xsi:schemaLocation", HEADER_XMLNS_XSI_SCHEMA_LOCATIOM);

        xmlWriter.startElement(GraphmlDocument.TAGNAME, attributes);
        if (doc.desc != null) {
            doc.desc.writeXml(xmlWriter);
        }

        if (doc.getKeys() != null) for (GraphmlKey key : doc.getKeys())
            data(key);

        writerData(doc.getDataList());
    }

    @Override
    public void startEdge(GraphmlEdge edge) throws IOException {
        // TODO implement
    }

    @Override
    public void startGraph(GraphmlGraph graphmlGraph) throws IOException {
        xmlWriter.startElement(GraphmlGraph.TAGNAME, graphmlGraph.getAttributes());
        if (graphmlGraph.desc != null) {
            graphmlGraph.desc.writeXml(xmlWriter);
        }
        writerData(graphmlGraph.getDataList());

    }

    @Override
    public void startHyperEdge(GraphmlHyperEdge edge) throws IOException {
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

    private void writerData(GraphmlData data) throws IOException {
        xmlWriter.startElement(GraphmlData.TAGNAME, data.getAttributes());
        xmlWriter.characterData(data.getValue());
        xmlWriter.endElement(GraphmlData.TAGNAME);
    }

    private void writerData(List<GraphmlData> datas) throws IOException {
        if (datas != null) for (GraphmlData data : datas) {
            writerData(data);
        }
    }
}
