package com.calpano.graphinout.base.output.xml;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.output.GraphMlWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphMlXmlWriter implements GraphMlWriter {
    private final XmlWriter xmlWriter;

    public GraphMlXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Override
    public void startGraphMl(GioDocument gioGraphML) throws IOException {
        xmlWriter.startDocument();
        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);
    }

    @Override
    public void startKey(GioKey gioKey) throws IOException {
        Map<String, String> keyAttribute = new LinkedHashMap<>();
        if (gioKey.getId() != null && !gioKey.getId().isEmpty()) keyAttribute.put("id", gioKey.getId());
        if (gioKey.getAttrName() != null && !gioKey.getAttrName().isEmpty())
            keyAttribute.put("attr.name", gioKey.getAttrName());
        if (gioKey.getAttrType() != null && !gioKey.getAttrType().isEmpty())
            keyAttribute.put("attr.type", gioKey.getAttrType());
        if (gioKey.getExtraAttrib() != null && !gioKey.getExtraAttrib().isEmpty())
            keyAttribute.put("extra.attrib", gioKey.getExtraAttrib());
        if (gioKey.getForType() != null) keyAttribute.put("for", gioKey.getForType().name().toLowerCase());

        xmlWriter.startElement(GioGraphInOutXMLConstants.KEY_ELEMENT_NAME, keyAttribute);
        if (gioKey.getDesc() != null) {
            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
            xmlWriter.characterData(gioKey.getDesc());
            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
        }
        if (gioKey.getDefaultValue() != null) {
            xmlWriter.startElement(GioGraphInOutXMLConstants.DEFAULT_ELEMENT_NAME);
            xmlWriter.characterData(gioKey.getDefaultValue().getValue());
            xmlWriter.endElement(GioGraphInOutXMLConstants.DEFAULT_ELEMENT_NAME);
        }

        //TODO check other attribute or element
    }

    @Override
    public void end(GioKey gioKey) throws IOException {
        xmlWriter.endElement(GioGraphInOutXMLConstants.KEY_ELEMENT_NAME);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        Map<String, String> keyAttribute = new LinkedHashMap<>();
        keyAttribute.put("id", gioGraph.getId());
        keyAttribute.put("edgedefault", gioGraph.isEdgedefault() + "");
        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME, keyAttribute);
        if (gioGraph.getDesc() != null) {
            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
            xmlWriter.characterData(gioGraph.getDesc());
            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
        }

    }

    @Override
    public void startNode(GioNode node) throws IOException {
        Map<String, String> keyAttribute = new LinkedHashMap<>();
        keyAttribute.put("id", node.getId());
        xmlWriter.startElement(GioGraphInOutXMLConstants.NODE_ELEMENT_NAME, keyAttribute);
        if (node.getDesc() != null) {
            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
            xmlWriter.characterData(node.getDesc());
            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
        }
        if (node.getPorts() != null) for (GioPort port : node.getPorts()) {
            keyAttribute = new LinkedHashMap<>();
            if (port.getName() != null) keyAttribute.put("name", port.getName());
            if (port.getExtraAttrib() != null) keyAttribute.put("port.extra.attrib", port.getExtraAttrib());

            xmlWriter.startElement(GioGraphInOutXMLConstants.PORT_ELEMENT_NAME, keyAttribute);

            xmlWriter.endElement(GioGraphInOutXMLConstants.PORT_ELEMENT_NAME);

        }
    }

    @Override
    public void endNode(GioNode node) throws IOException {
        xmlWriter.endElement("node");
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        Map<String, String> keyAttribute = new LinkedHashMap<>();
        if (edge.getId() != null && !edge.getId().isEmpty()) keyAttribute.put("id", edge.getId());
        if (edge.getExtraAttrib() != null && !edge.getExtraAttrib().isEmpty())
            keyAttribute.put("hyperEdge.extra.attrib", edge.getExtraAttrib());
        xmlWriter.startElement(GioGraphInOutXMLConstants.HYPER_EDGE_ELEMENT_NAME, keyAttribute);
        if (edge.getEndpoints() != null)
            for (GioEndpoint gioEndpoint : edge.getEndpoints()) {
                keyAttribute = new LinkedHashMap<>();
                if (gioEndpoint.getId() != null && !gioEndpoint.getId().isEmpty())
                    keyAttribute.put("id", gioEndpoint.getId());
                if (gioEndpoint.getPort() != null && !gioEndpoint.getPort().isEmpty())
                    keyAttribute.put("port", gioEndpoint.getPort());
                if (gioEndpoint.getNode() != null && !gioEndpoint.getNode().isEmpty())
                    keyAttribute.put("node", gioEndpoint.getNode());
                if (gioEndpoint.getType() != null) keyAttribute.put("type", gioEndpoint.getType().name().toLowerCase());
                xmlWriter.startElement(GioGraphInOutXMLConstants.ENDPOINT_ELEMENT_NAME, keyAttribute);
                if (gioEndpoint.getDesc() != null) {
                    xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
                    xmlWriter.characterData(gioEndpoint.getDesc().getValue());
                    xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
                }
                xmlWriter.endElement(GioGraphInOutXMLConstants.ENDPOINT_ELEMENT_NAME);
            }


    }

    @Override
    public void endEdge(GioEdge gioHyperEdge) throws IOException {
        xmlWriter.endElement(GioGraphInOutXMLConstants.HYPER_EDGE_ELEMENT_NAME);
    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {
        xmlWriter.endElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME);
    }

    @Override
    public void endGraphMl(GioDocument gioGraphML) throws IOException {
        xmlWriter.endElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);
        xmlWriter.endDocument();
    }
}