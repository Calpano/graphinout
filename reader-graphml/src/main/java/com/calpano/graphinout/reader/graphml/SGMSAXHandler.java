package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.GioData;
import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.GioGraphML;
import com.calpano.graphinout.base.GioNode;
import com.calpano.graphinout.base.*;

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
    private GioData currentData;

    private GioHyperEdge currentGioHyperEdge;

    private List<GioNode> invalidNode = new ArrayList<>();

    public SGMSAXHandler(File outPutFile) throws FileNotFoundException {
        this.outPutFile = outPutFile;
        fileOutputStream = new FileOutputStream(outPutFile);
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
            startElementNodeData(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("port")) {
            if (elementsNames.peek().endsWith("node")) {
                currentElement = "nodePort";
                startElementNodePort(uri, localName, qName, attributes);
            }

        } else if (qName.equalsIgnoreCase("edge")) {
            startElementEdge(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("hyperedge")) {
            startElementHyperEdge(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("endpoint")) {
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

        GioKey key =  GioKey.builder().attrName(name).attrType(type).id(id).build();
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
        currentData = new GioData();
        currentData.setKey(key);
        currenGioNode.addData(currentData);

    }

    private void startElementNodePort(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String name = attributes.getValue("name");
        GioPort gioPort =  GioPort.builder().name(name).build();
        if (currenGioNode.getPorts() == null || currenGioNode.getPorts().isEmpty()) {
            currenGioNode.setPorts(new ArrayList<>());
        }
        currenGioNode.getPorts().add(currenGioNode.getPorts().size(), gioPort);

    }

    private void startElementEdge(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        String directed = attributes.getValue("directed");
        currentGioEdge = new GioEdge();
        currentGioEdge.setId(id);
        currentGioEdge.setDirected(Boolean.getBoolean(directed));
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
        if (currentGioGraph.getHyperEdges() == null) {
            currentGioGraph.setHyperEdges(new ArrayList<>());
        }

        currentGioGraph.getHyperEdges().add(currentGioGraph.getHyperEdges().size(), currentGioEdge.hyperEdge());

    }


    private void startElementHyperEdge(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String id = attributes.getValue("id");
        currentGioHyperEdge = GioHyperEdge.builder().id(id).build();

        if (currentGioGraph.getHyperEdges() == null) {
            currentGioGraph.setHyperEdges(new ArrayList<>());
        }
        currentGioGraph.getHyperEdges().add(currentGioGraph.getHyperEdges().size(), currentGioHyperEdge);
    }

    private void startElementHyperEdgeEndpoint(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        String type = attributes.getValue("type");
        GioEndpoint gioEndpoint = GioEndpoint.builder().id(id).type(Direction.getDirection(type)).build();

        String node = attributes.getValue("node");
        if (node != null) {
            gioEndpoint.setNode(findGioNodeInCurrentGraph(node).getId());
        }
        String port = attributes.getValue("port");
        if (port != null) {
            gioEndpoint.setPort(findGioPortInCurrentGraph(port).getName());
        }

        if (currentGioHyperEdge.getEndpoints() == null || currentGioHyperEdge.getEndpoints().isEmpty()) {
            currentGioHyperEdge.setEndpoints(new ArrayList<>());
        }
        currentGioHyperEdge.addEndpoint(gioEndpoint);
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentData != null) currentData.setValue(new String(ch, start, length));
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
        } else return tmp.get();
    }

    private GioPort findGioPortInCurrentGraph(String port) {
        return  GioPort.builder().name(port).build();
    }
}
