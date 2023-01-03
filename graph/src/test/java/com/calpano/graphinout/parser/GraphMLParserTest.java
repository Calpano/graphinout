/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.calpano.graphinout.parser;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioEdge;
import com.calpano.graphinout.graph.GioEdgeData;
import com.calpano.graphinout.graph.GioEndpoint;
import com.calpano.graphinout.graph.GioGraph;
import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.graph.GioHyperEdge;
import com.calpano.graphinout.graph.GioKey;
import com.calpano.graphinout.graph.GioNode;
import com.calpano.graphinout.graph.GioNodeData;
import com.calpano.graphinout.graph.GioPort;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author rbaba
 */
public class GraphMLParserTest {

    private final static GioGraphML DEFUALT_GIO_GRAPH = createDefualtGioGraphML();

    /**
     * Test of unmarshall method, of class GraphMLParserImpl.
     */
    @Test
    public void testUnmarshall() throws Exception {
        System.out.println("unmarshall");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "test").toString() + "/graphml.xml";
        GraphMLParserImpl instance = new GraphMLParserImpl();
        GioGraphML expResult = DEFUALT_GIO_GRAPH;
        GioGraphML result = instance.unmarshall(fileName);
        assertEquals(expResult, result);

    }

    /**
     * Test of marshall method, of class GraphMLParserImpl.
     */
    @Test
    public void testMarshall() throws Exception {
        System.out.println("marshall");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "test").toString() + "/graphml.xml";

        GioGraphML graphML = DEFUALT_GIO_GRAPH;
        GraphMLParserImpl instance = new GraphMLParserImpl();
        instance.marshall(fileName, graphML);

    }

    private static GioGraphML createDefualtGioGraphML() {

        GioKey gioKey1 = new GioKey("test1", "test1", "test1", "default");
        GioKey gioKey2 = new GioKey("test2", "test2", "test2", "");
        GioKey gioKey3 = new GioKey("test3", "test3", "test3", null);

        List<GioKey> gioKeys = new ArrayList<>();
        gioKeys.add(gioKey1);
        gioKeys.add(gioKey2);
        gioKeys.add(gioKey3);
        GioGraphML temp = GioGraphML.builder().keys(gioKeys).build();

        List<GioNode> nodes1 = new ArrayList<>();
        GioNodeData dataString1 = new GioNodeData("dataString1key", "dataString1");
        GioNodeData dataString2 = new GioNodeData("dataString2key", "dataString2");
        GioNodeData dataString3 = new GioNodeData("dataString2key", "dataString3");
        GioNode gioNode11 = new GioNode(dataString1, "gioNode11", null);
        GioNode gioNode12 = new GioNode(dataString2, "gioNode12", null);
        GioNode gioNode13 = new GioNode(dataString3, "gioNode13", null);
        nodes1.add(gioNode11);
        nodes1.add(gioNode12);
        nodes1.add(gioNode13);

        GioEdgeData gioEdgeData1 = GioEdgeData.builder().value("gioEdgeData1").key(gioKey1).build();
        GioEdgeData gioEdgeData2 = GioEdgeData.builder().value("gioEdgeData2").key(gioKey2).build();
        GioEdgeData gioEdgeData3 = GioEdgeData.builder().value("gioEdgeData3").key(gioKey3).build();

        List<GioEdge> edges1 = new ArrayList<>();
        GioEdge gioEdge1 = GioEdge.builder().data(gioEdgeData1).source(gioNode11).target(gioNode12).build();
        GioEdge gioEdge2 = GioEdge.builder().data(gioEdgeData2).source(gioNode12).target(gioNode13).build();
        GioEdge gioEdge3 = GioEdge.builder().data(gioEdgeData3).source(gioNode13).target(gioNode11).build();
        edges1.add(gioEdge1);
        edges1.add(gioEdge2);
        edges1.add(gioEdge3);
        GioGraph gioGraph1 = new GioGraph("edgedefault", "gioGraph1", nodes1, edges1, null);
        List<GioGraph> gioGraphs = new ArrayList<>();
        gioGraphs.add(gioGraph1);

        temp.setGraphs(gioGraphs);
        return temp;
    }

    @Test

    public void graph_A_test() throws GioException {
        System.out.println("graph_A_test");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graph-a.graphml";

        GraphMLParserImpl instance = new GraphMLParserImpl();
        GioGraphML result = instance.unmarshall(fileName);
        assertEquals(1, result.getKeys().size());

        assertEquals("type", result.getKeys().get(0).getAttrName());
        assertEquals("string", result.getKeys().get(0).getAttrType());
        assertEquals("type", result.getKeys().get(0).getId());
        List<GioGraph> gioGraphs = result.getGraphs();
        assertEquals(1, gioGraphs.size());
        GioGraph gioGraph = gioGraphs.get(0);
        List<GioNode> gioNodes = gioGraph.getNodes();
        assertEquals(7, gioNodes.size());
        GioNode gioNode0 = gioNodes.get(0);
        assertEquals("cat", gioNode0.getId());
        assertEquals("label", gioNode0.getData().getKey());
        assertEquals("cat", gioNode0.getData().getValue());

        List<GioEdge> gioEdges = gioGraph.getEdges();
        assertEquals(11, gioEdges.size());
        GioEdge gioEdge0 = gioEdges.get(0);
        assertEquals("SAP", gioEdge0.getSource().getId());
        assertEquals("Company", gioEdge0.getTarget().getId());
        assertEquals("type", gioEdge0.getData().getKey().getAttrName());
        assertEquals("string", gioEdge0.getData().getKey().getAttrType());
        assertEquals("type", gioEdge0.getData().getKey().getId());
        assertEquals("hasType", gioEdge0.getData().getValue());

    }

    @Test

    public void graph_B_test() throws GioException {
        System.out.println("graph_B_test");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graph-b.graphml";

        GraphMLParserImpl instance = new GraphMLParserImpl();
        GioGraphML result = instance.unmarshall(fileName);
        assertEquals(1, result.getKeys().size());
        assertEquals("type", result.getKeys().get(0).getAttrName());
        assertEquals("string", result.getKeys().get(0).getAttrType());
        assertEquals("type", result.getKeys().get(0).getId());
        List<GioGraph> gioGraphs = result.getGraphs();
        assertEquals(1, gioGraphs.size());
        GioGraph gioGraph = gioGraphs.get(0);
        assertEquals("", gioGraph.getId());
        assertEquals("directed", gioGraph.getEdgedefault());

        List<GioNode> gioNodes = gioGraph.getNodes();
        assertEquals(3, gioNodes.size());
        GioNode gioNode0 = gioNodes.get(0);
        assertEquals("s1", gioNode0.getId());
        assertEquals("label", gioNode0.getData().getKey());
        assertEquals("Source 1", gioNode0.getData().getValue());

        GioNode gioNode1 = gioNodes.get(1);
        assertEquals("t1", gioNode1.getId());
        assertEquals("label", gioNode1.getData().getKey());
        assertEquals("Target 1", gioNode1.getData().getValue());

        GioNode gioNode2 = gioNodes.get(2);
        assertEquals("t2", gioNode2.getId());
        assertEquals("label", gioNode2.getData().getKey());
        assertEquals("Target 2", gioNode2.getData().getValue());

        List<GioEdge> gioEdges = gioGraph.getEdges();
        assertEquals(2, gioEdges.size());
        GioEdge gioEdge0 = gioEdges.get(0);
        assertEquals("s1", gioEdge0.getSource().getId());
        assertEquals("t1", gioEdge0.getTarget().getId());
        assertEquals("type", gioEdge0.getData().getKey().getAttrName());
        assertEquals("string", gioEdge0.getData().getKey().getAttrType());
        assertEquals("type", gioEdge0.getData().getKey().getId());
        assertEquals("directed", gioEdge0.getData().getValue());

        GioEdge gioEdge1 = gioEdges.get(1);
        assertEquals("false", gioEdge1.getDirected());
        assertEquals("s1", gioEdge1.getSource().getId());
        assertEquals("t2", gioEdge1.getTarget().getId());
        assertEquals("type", gioEdge1.getData().getKey().getAttrName());
        assertEquals("string", gioEdge1.getData().getKey().getAttrType());
        assertEquals("type", gioEdge1.getData().getKey().getId());
        assertEquals("undirected", gioEdge1.getData().getValue());

    }

    @Test
    @Disabled
    public void graphml_ids_test() throws GioException {
        System.out.println("graphml_ids_test");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml-ids.xml";

        GraphMLParserImpl instance = new GraphMLParserImpl();
        GioGraphML result = instance.unmarshall(fileName);

        List<GioGraph> gioGraphs = result.getGraphs();
        assertEquals(1, gioGraphs.size());
        GioGraph gioGraph = gioGraphs.get(0);
        assertEquals("id--graph", gioGraph.getId());
        assertEquals("directed", gioGraph.getEdgedefault());

        List<GioNode> gioNodes = gioGraph.getNodes();
        assertEquals(6, gioNodes.size());
        GioNode gioNode0 = gioNodes.get(0);
        assertEquals("id--node1", gioNode0.getId());

        GioNode gioNode1 = gioNodes.get(1);
        assertEquals("id--node2", gioNode1.getId());

        GioNode gioNode2 = gioNodes.get(2);
        assertEquals("id--node3", gioNode2.getId());

        GioNode gioNode3 = gioNodes.get(3);
        assertEquals("id--node4", gioNode3.getId());
        List<GioPort> gioPorts3 = gioNode3.getPorts();
        assertEquals(0, gioPorts3.size());
        GioPort gioPort31 = gioPorts3.get(0);
        assertEquals("North", gioPort31.getName());
        GioPort gioPort32 = gioPorts3.get(1);
        assertEquals("South", gioPort32.getName());

        GioNode gioNode4 = gioNodes.get(4);
        assertEquals("id--node5", gioNode4.getId());

        List<GioPort> gioPorts4 = gioNode4.getPorts();
        assertEquals(2, gioPorts4.size());
        GioPort gioPort41 = gioPorts4.get(0);
        assertEquals("North", gioPort41.getName());
        GioPort gioPort42 = gioPorts4.get(1);
        assertEquals("South", gioPort42.getName());

        GioNode gioNode5 = gioNodes.get(5);
        assertEquals("id--node3", gioNode5.getId());

        List<GioPort> gioPorts5 = gioNode5.getPorts();
        assertEquals(2, gioPorts5.size());
        GioPort gioPort51 = gioPorts5.get(0);
        assertEquals("North", gioPort51.getName());
        GioPort gioPort52 = gioPorts5.get(1);
        assertEquals("South", gioPort52.getName());

        List<GioEdge> gioEdges = gioGraph.getEdges();
        assertEquals(2, gioEdges.size());
        GioEdge gioEdge0 = gioEdges.get(0);
        assertEquals("id--edge-12", gioEdge0.getId());
        assertEquals("id--node1", gioEdge0.getSource().getId());
        assertEquals("id--node2", gioEdge0.getTarget().getId());

        GioEdge gioEdge1 = gioEdges.get(1);
        assertEquals("id--edge-4S6N", gioEdge0.getId());
        assertEquals("id--node4", gioEdge1.getSource().getId());
        assertEquals("id--node6", gioEdge1.getTarget().getId());
        assertEquals("South", gioEdge1.getSourcePort().getName());
        assertEquals("North", gioEdge1.getTargetPort().getName());

        List<GioHyperEdge> gioHyperEdges = gioGraph.getHyperEdges();
        assertEquals(2, gioHyperEdges.size());
        GioHyperEdge gioHyperEdge1 = gioHyperEdges.get(0);
        assertEquals("id--hyperedge-4N56", gioHyperEdge1.getId());
        List<GioEndpoint> gioEndpoints1 = gioHyperEdge1.getEndpoints();
        assertEquals(3, gioEndpoints1.size());

        GioEndpoint gioEndpoint10 = gioEndpoints1.get(0);
        assertEquals("id--node1", gioEndpoint10.getNode().getId());
        assertEquals("in", gioEndpoint10.getType());

        GioEndpoint gioEndpoint11 = gioEndpoints1.get(1);
        assertEquals("id--node2", gioEndpoint11.getNode().getId());
        assertEquals("out", gioEndpoint11.getType());

        GioEndpoint gioEndpoint12 = gioEndpoints1.get(2);
        assertEquals("id--node3", gioEndpoint12.getNode().getId());
        assertEquals("undir", gioEndpoint12.getType());

        GioEndpoint gioEndpoint20 = gioEndpoints1.get(1);
        assertEquals("id--node4", gioEndpoint20.getNode().getId());
        assertEquals("in", gioEndpoint20.getType());
        assertEquals("North", gioEndpoint20.getPort().getName());
        GioEndpoint gioEndpoint21 = gioEndpoints1.get(1);
        assertEquals("id--node5", gioEndpoint21.getNode().getId());
        assertEquals("in", gioEndpoint21.getType());

        GioEndpoint gioEndpoint22 = gioEndpoints1.get(1);
        assertEquals("id--node6", gioEndpoint22.getNode().getId());
        assertEquals("in", gioEndpoint22.getType());

    }

}
