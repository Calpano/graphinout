//package com.calpano.graphinout.base.output.xml;
//
//import com.calpano.graphinout.base.gio.*;
//import com.calpano.graphinout.base.graphml.GraphmlHyperEdge;
//import com.calpano.graphinout.base.output.GraphMlWriter;
//
//import java.io.IOException;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//public class GraphMlXmlWriter implements GraphMlWriter {
//    private final   XmlWriter xmlWriter;
//
//    public GraphMlXmlWriter(XmlWriter xmlWriter) {
//        this.xmlWriter = xmlWriter;
//    }
//
//    @Override
//    public void startGraphMl(GioDocument gioGraphML) throws IOException {
//        xmlWriter.startDocument();
//        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);
//    }
//
//    @Override
//    public void startKey(GioKey gioKey) throws IOException {
//        Map<String,String> keyAttribute = new LinkedHashMap<>();
//        keyAttribute.put("id",gioKey.getId());
//        keyAttribute.put("attr.name",gioKey.getAttrName());
//        keyAttribute.put("attr.type",gioKey.getAttrType());
//        keyAttribute.put("extra.attrib",gioKey.getExtraAttrib());
//        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME,keyAttribute);
//        if(gioKey.getDesc()!=null)
//        {
//            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//            xmlWriter.characterData(gioKey.getDesc());
//            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//        }
//        //TODO check other attribute or element
//    }
//
//    @Override
//    public void end(GioKey gioKey) throws IOException {
//        xmlWriter.endElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);
//    }
//
//    @Override
//    public void startGraph(GioGraph gioGraph) throws IOException {
//        Map<String,String> keyAttribute = new LinkedHashMap<>();
//        keyAttribute.put("id",gioGraph.getId());
//        keyAttribute.put("edgedefault",gioGraph.isEdgedefault()+"");
//        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME,keyAttribute);
//        if(gioGraph.getDesc()!=null)
//        {
//            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//            xmlWriter.characterData(gioGraph.getDesc());
//            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//        }
//
//    }
//
//    @Override
//    public void startNode(GioNode node) throws IOException {
//        Map<String,String> keyAttribute = new LinkedHashMap<>();
//        keyAttribute.put("id",node.getId());
//        xmlWriter.startElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME,keyAttribute);
//        if(node.getDesc()!=null)
//        {
//            xmlWriter.startElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//            xmlWriter.characterData(node.getDesc());
//            xmlWriter.endElement(GioGraphInOutXMLConstants.DESC_ELEMENT_NAME);
//        }
//    }
//
//    @Override
//    public void endNode(GioNode node) throws IOException {
//        xmlWriter.endElement("node");
//    }
//
//    @Override
//    public void startEdge(GioEdge edge) throws IOException {
//        GraphmlHyperEdge hyperEdge =  new GraphmlHyperEdge();
//        hyperEdge.
//    }
//
//    @Override
//    public void endEdge(GioEdge gioHyperEdge) throws IOException {
//
//    }
//
//    @Override
//    public void endGraph(GioGraph gioGraph) throws IOException {
//
//    }
//
//    @Override
//    public void endGraphMl(GioDocument gioGraphML) throws IOException {
//
//    }
//}
