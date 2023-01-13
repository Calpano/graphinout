package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.GioGraphInOutConstants;
import com.calpano.graphinout.graph.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

import lombok.Data;

/**
 * @author rbaba
 */
@Data
@Slf4j
public class SGMSAXHandler extends DefaultHandler {


    private final File outPutFile;
    private final FileOutputStream fileOutputStream;

    private Stack<String> elementsNames = new Stack<>();
    private String currentElement;
    private GioGraphML gioGraphML = new GioGraphML();
    private GioGraph currentGioGraph;
    private GioNode currenGioNode;
    private GioEdge currentGioEdge;
    private GioNodeData currentNodeData;

    private GioHyperEdge currentGioHyperEdge;

    private List<GioNode> invalidNode = new ArrayList<>();

    public SGMSAXHandler(File outPutFile) throws FileNotFoundException {
        this.outPutFile = outPutFile;
        fileOutputStream =  new FileOutputStream(outPutFile);
    }




    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        log.debug(GioGraphInOutConstants.START_LOG);
        log.debug("Start process Element :  {}", qName);
        currentElement = qName;
        if (qName.equalsIgnoreCase("graphml")) {
            startElementGraphML(uri, localName, qName, attributes);

        } else if (qName.equalsIgnoreCase("key")) {
            startElementKey(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("graph")) {
            startElementGraph(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("node")) {
            startElementNode(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("data")) {
            if (elementsNames.peek().endsWith("node")) {
                currentElement = "nodeData";
                startElementNodeData(uri, localName, qName, attributes);

            } else if (elementsNames.peek().endsWith("edge")) {
                currentElement = "edgeData";
            }
        } else if (qName.equalsIgnoreCase("port")) {
            if (elementsNames.peek().endsWith("node")) {
                currentElement = "nodePort";
                startElementNodePort(uri, localName, qName, attributes);
            }

        } else if (qName.equalsIgnoreCase("edge")) {
            startElementEdge(uri, localName, qName, attributes);
        } else if(qName.equalsIgnoreCase("hyperedge")){
            startElementHyperEdge(uri, localName, qName, attributes);
        }else if(qName.equalsIgnoreCase("endpoint")){
            startElementHyperEdgeEndpoint(uri, localName, qName, attributes);
        }
        elementsNames.add(qName.toLowerCase());
        super.startElement(uri, localName, qName, attributes);
    }

    private void startElementGraphML(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        gioGraphML.setId(id);

    }

    private void startElementKey(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String name = attributes.getValue("attr.name");
        String type = attributes.getValue("attr.type");
        String id = attributes.getValue("id");
        String defaultValue = attributes.getValue("default");
        GioKey key = new GioKey(name, type, id, defaultValue);
        if (gioGraphML.getKeys() == null) {
            gioGraphML.setKeys(new ArrayList<>());
        }
        gioGraphML.getKeys().add(key);

    }

    private void startElementGraph(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String edgedefault = attributes.getValue("edgedefault");
        String id = attributes.getValue("id");
        if (gioGraphML.getGraphs() == null) {
            gioGraphML.setGraphs(new ArrayList<>());
        }
        currentGioGraph = new GioGraph(id, edgedefault);
        gioGraphML.getGraphs().add(gioGraphML.getGraphs().size(), currentGioGraph);

    }

    private void startElementNode(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        currenGioNode = invalidNode.stream().filter((t) -> t.getId().equals(id)).findFirst().orElse(GioNode.builder().id(id).build());
        invalidNode.removeIf((t) -> t.getId().equals(id));
        if (gioGraphML.getGraphs().get(gioGraphML.getGraphs().size() - 1) == null) {
            //TODO error
        }
        if (currentGioGraph.getNodes() == null) {
            currentGioGraph.setNodes(new ArrayList<>());
        }

        currentGioGraph.getNodes().add(currentGioGraph.getNodes().size(), currenGioNode);

    }

    private void startElementNodeData(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String key = attributes.getValue("key");
        currentNodeData = new GioNodeData();
        currentNodeData.setKey(key);
        currenGioNode.setData(currentNodeData);

    }

    private void startElementNodePort(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String name = attributes.getValue("name");
        GioPort gioPort = new GioPort(name);
        if (currenGioNode.getPorts() == null || currenGioNode.getPorts().isEmpty() ) {
            currenGioNode.setPorts(new ArrayList<>());
        }
        currenGioNode.getPorts().add(currenGioNode.getPorts().size(), gioPort);

    }

    private void startElementEdge(String uri, String localName, String qName, Attributes attributes) throws SAXException {

//    @XmlElement(required = true)
//    protected GioEdgeData data;
//   
        String id = attributes.getValue("id");
        String directed = attributes.getValue("directed");
        currentGioEdge = new GioEdge();
        currentGioEdge.setId(id);
        currentGioEdge.setDirected(directed);
        String source = attributes.getValue("source");
        if (source != null) {
            currentGioEdge.setSource(findGioNodeInCurrentGraph(source));
        }
        String target = attributes.getValue("target");
        if (target != null) {
            currentGioEdge.setTarget(findGioNodeInCurrentGraph(target));
        }
        String sourceport = attributes.getValue("sourceport");
        if (sourceport != null) {
            currentGioEdge.setSourcePort(findGioPortInCurrentGraph(sourceport));
        }
        String targetport = attributes.getValue("targetport");
        if (targetport != null) {
            currentGioEdge.setTargetPort(findGioPortInCurrentGraph(targetport));
        }
        String port = attributes.getValue("port");
        if (port != null) {
            currentGioEdge.setSourcePort(findGioPortInCurrentGraph(port));
        }

        if (currentGioGraph.getEdges() == null) {
            currentGioGraph.setEdges(new ArrayList<>());
        }
        currentGioGraph.getEdges().add(currentGioGraph.getEdges().size(), currentGioEdge);

    }


    private void startElementHyperEdge(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String id = attributes.getValue("id");
        currentGioHyperEdge =  GioHyperEdge.builder().id(id).build();

        if(currentGioGraph.getHyperEdges()==null){
            currentGioGraph.setHyperEdges(new ArrayList<>());
        }
        currentGioGraph.getHyperEdges().add(currentGioGraph.getHyperEdges().size(),currentGioHyperEdge);
    }

    private void startElementHyperEdgeEndpoint(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        String type = attributes.getValue("type");
        GioEndpoint gioEndpoint = GioEndpoint.builder().id(id).type(type).build();

        String node = attributes.getValue("node");
        if(node!=null){
        gioEndpoint.setNode(findGioNodeInCurrentGraph(node));
       }
        String port = attributes.getValue("port");
        if(port!=null){
            gioEndpoint.setPort(findGioPortInCurrentGraph(port));
        }

        if(currentGioHyperEdge.getEndpoints()==null ||
                currentGioHyperEdge.getEndpoints().isEmpty()) {
            currentGioHyperEdge.setEndpoints(new ArrayList<>());
        }
        currentGioHyperEdge.getEndpoints().add(gioEndpoint);
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement.equalsIgnoreCase("nodeData")) {
            currentNodeData.setValue(new String(ch, start, length));
        }
        super.characters(ch, start, length);

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (!elementsNames.peek().equalsIgnoreCase(qName)) {
            //TODO manager error
        }
        elementsNames.pop();


        super.endElement(uri, localName, qName);

        log.debug("end process Elemen {}", qName);
        log.debug(GioGraphInOutConstants.END_LOG);
    }

    private GioNode findGioNodeInCurrentGraph(String node) {
        Optional<GioNode> tmp = currentGioGraph.getNodes().stream().filter((t) -> t.getId().equalsIgnoreCase(node)).findFirst();
        if (!tmp.isPresent()) {
            GioNode tmp2 = GioNode.builder().id(node).build();
            invalidNode.add(tmp2);
            return tmp2;
        }else
        return tmp.get();
    }

    private GioPort findGioPortInCurrentGraph(String port) {
        return new GioPort(port);
    }
}
