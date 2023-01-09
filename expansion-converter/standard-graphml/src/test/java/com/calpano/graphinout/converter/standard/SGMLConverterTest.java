package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.converter.standard.xml.StandardGraphML;
import com.calpano.graphinout.graph.*;

import com.calpano.graphinout.graphml.GraphMLService;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author rbaba
 */
public class SGMLConverterTest {


    @DisplayName("Load file with GioStandardGMLService and test field by field successfully pass.")
    @Nested
    class successfulTest {
        /**
         * Test of convert method, of class SGMLConverter.
         */
        @DisplayName("Load file graphml-ids.xml")
        @Test
        public void testConvert_graphml_ids() throws Exception {


            String xmlFile = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml-ids.xml";

            GraphMLService<GioGraphML> myGMLService = new GioStandardGMLService();
            SGMLConverter instance = new SGMLConverter();

            GioGraphML result = instance.convert(new File(xmlFile), myGMLService);
            assertNull(result.getId());
            assertNull(result.getKeys());
            assertEquals(1, result.getGraphs().size());

            GioGraph gioGraph = result.getGraphs().get(0);
            assertEquals("id--graph", gioGraph.getId());
            assertEquals("directed", gioGraph.getEdgedefault());

            List<GioNode> gioNodes = gioGraph.getNodes();
            assertEquals(6, gioNodes.size());

            GioNode gioNode1 = gioNodes.get(0);
            assertEquals("id--node1", gioNode1.getId());
            assertNull(gioNode1.getData());
            assertEquals(0, gioNode1.getPorts().size());

            GioNode gioNode2 = gioNodes.get(1);
            assertEquals("id--node2", gioNode2.getId());
            assertNull(gioNode2.getData());
            assertEquals(0, gioNode2.getPorts().size());

            GioNode gioNode3 = gioNodes.get(2);
            assertEquals("id--node3", gioNode3.getId());
            assertNull(gioNode3.getData());
            assertEquals(0, gioNode3.getPorts().size());


            GioNode gioNode4 = gioNodes.get(3);
            assertEquals("id--node4", gioNode4.getId());
            assertNull(gioNode4.getData());
            assertEquals(2, gioNode4.getPorts().size());

            GioPort gioPort41 = gioNode4.getPorts().get(0);
            assertEquals("North", gioPort41.getName());

            GioPort gioPort42 = gioNode4.getPorts().get(1);
            assertEquals("South", gioPort42.getName());

            GioNode gioNode5 = gioNodes.get(4);
            assertEquals("id--node5", gioNode5.getId());
            assertNull(gioNode5.getData());
            assertEquals(2, gioNode5.getPorts().size());

            GioPort gioPort51 = gioNode5.getPorts().get(0);
            assertEquals("North", gioPort51.getName());

            GioPort gioPort52 = gioNode5.getPorts().get(1);
            assertEquals("South", gioPort52.getName());

            GioNode gioNode6 = gioNodes.get(5);
            assertEquals("id--node6", gioNode6.getId());
            assertNull(gioNode6.getData());
            assertEquals(2, gioNode6.getPorts().size());

            GioPort gioPort61 = gioNode6.getPorts().get(0);
            assertEquals("North", gioPort61.getName());

            GioPort gioPort62 = gioNode6.getPorts().get(1);
            assertEquals("South", gioPort62.getName());


            List<GioEdge> gioEdges = gioGraph.getEdges();
            assertEquals(2, gioEdges.size());

            GioEdge gioEdge1 = gioEdges.get(0);
            assertEquals("id--edge-12", gioEdge1.getId());
            assertEquals(gioNode1.getId(), gioEdge1.getSource().getId());
            assertEquals(gioNode2.getId(), gioEdge1.getTarget().getId());
            assertNull(gioEdge1.getSourcePort());
            assertNull(gioEdge1.getTargetPort());
            assertNull(gioEdge1.getData());
            assertNull(gioEdge1.getDirected());


            GioEdge gioEdge2 = gioEdges.get(1);
            assertEquals("id--edge-4S6N", gioEdge2.getId());
            assertEquals(gioNode4, gioEdge2.getSource());
            assertEquals(gioNode6, gioEdge2.getTarget());
            assertEquals(new GioPort("South"), gioEdge2.getSourcePort());
            assertEquals(new GioPort("North"), gioEdge2.getTargetPort());
            assertNull(gioEdge2.getData());
            assertNull(gioEdge2.getDirected());

            List<GioHyperEdge> hyperEdges = gioGraph.getHyperEdges();
            assertEquals(2, hyperEdges.size());

            GioHyperEdge gioHyperEdge1 = hyperEdges.get(0);
            assertEquals("id--hyperedge-123", gioHyperEdge1.getId());

            List<GioEndpoint> gioEndpoints11 = gioHyperEdge1.getEndpoints();
            assertEquals(3, gioEndpoints11.size());

            GioEndpoint gioEndpoint111 = gioEndpoints11.get(0);
            assertNull(gioEndpoint111.getId());
            assertEquals("in", gioEndpoint111.getType());
            assertEquals(gioNode1, gioEndpoint111.getNode());
            assertNull(gioEndpoint111.getPort());

            GioEndpoint gioEndpoint112 = gioEndpoints11.get(1);
            assertNull(gioEndpoint112.getId());
            assertEquals("out", gioEndpoint112.getType());
            assertEquals(gioNode2, gioEndpoint112.getNode());
            assertNull(gioEndpoint112.getPort());

            GioEndpoint gioEndpoint113 = gioEndpoints11.get(2);
            assertNull(gioEndpoint113.getId());
            assertEquals("undir", gioEndpoint113.getType());
            assertEquals(gioNode3, gioEndpoint113.getNode());
            assertNull(gioEndpoint113.getPort());

            GioHyperEdge gioHyperEdge2 = hyperEdges.get(1);
            assertEquals("id--hyperedge-4N56", gioHyperEdge2.getId());

            List<GioEndpoint> gioEndpoints12 = gioHyperEdge2.getEndpoints();
            assertEquals(3, gioEndpoints12.size());

            GioEndpoint gioEndpoint121 = gioEndpoints12.get(0);
            assertNull(gioEndpoint121.getId());
            assertEquals("in", gioEndpoint121.getType());
            assertEquals(gioNode4.getId(), gioEndpoint121.getNode().getId());
            assertEquals(new GioPort("North"), gioEndpoint121.getPort());


            GioEndpoint gioEndpoint122 = gioEndpoints12.get(1);
            assertNull(gioEndpoint122.getId());
            assertEquals("in", gioEndpoint122.getType());
            assertEquals(gioNode5.getId(), gioEndpoint122.getNode().getId());
            assertNull(gioEndpoint122.getPort());

            GioEndpoint gioEndpoint123 = gioEndpoints12.get(2);
            assertNull(gioEndpoint123.getId());
            assertEquals("in", gioEndpoint123.getType());
            assertEquals(gioNode6.getId(), gioEndpoint123.getNode().getId());
            assertNull(gioEndpoint123.getPort());


        }

        @DisplayName("Load file graph-a.graphml")
        @Test
        @Disabled
        public void testConvert_graph_a_graphml() throws Exception {
            String xmlFile = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graph-a.graphml";

            GraphMLService<GioGraphML> myGMLService = new GioStandardGMLService();
            SGMLConverter instance = new SGMLConverter();

            GioGraphML result = instance.convert(new File(xmlFile), myGMLService);

              List<GioKey> keys =  result.getKeys();
              assertEquals(1,keys.size());
              GioKey gioKey1 = keys.get(0);
               assertEquals("type",gioKey1.getId());
            assertEquals("type",gioKey1.getAttrName());
            assertEquals("string",gioKey1.getAttrType());
            assertNull(gioKey1.getDefaultValue());

            assertEquals(1,result.getGraphs().size());
            GioGraph gioGraph =  result.getGraphs().get(0);

            assertEquals("directed",gioGraph.getEdgedefault());
            assertEquals("",gioGraph.getId());


            List<GioNode> gioNodes = gioGraph.getNodes();
            assertEquals(7, gioNodes.size());

            GioNode gioNode0 = gioNodes.get(0);
            assertEquals("cat", gioNode0.getId());
            assertEquals("label", gioNode0.getData().getKey());
            assertEquals("cat", gioNode0.getData().getValue());


            GioNode gioNode1 = gioNodes.get(1);
            assertEquals("ClaudiaStern", gioNode1.getId());
            assertEquals("label", gioNode1.getData().getKey());
            assertEquals("Claudia Stern", gioNode1.getData().getValue());


            GioNode gioNode2 = gioNodes.get(2);
            assertEquals("ClaudiCat", gioNode2.getId());
            assertEquals("label", gioNode2.getData().getKey());
            assertEquals("ClaudiCat", gioNode2.getData().getValue());

            GioNode gioNode3 = gioNodes.get(3);
            assertEquals("DirkHagemann", gioNode3.getId());
            assertEquals("label", gioNode3.getData().getKey());
            assertEquals("Dirk Hagemann", gioNode3.getData().getValue());
            GioNode gioNode4 = gioNodes.get(4);
            assertEquals("Company", gioNode4.getId());
            assertEquals("label", gioNode4.getData().getKey());
            assertEquals("Company", gioNode4.getData().getValue());


            GioNode gioNode5 = gioNodes.get(5);
            assertEquals("Person", gioNode5.getId());
            assertEquals("label", gioNode5.getData().getKey());
            assertEquals("Person", gioNode5.getData().getValue());


            GioNode gioNode6 = gioNodes.get(6);
            assertEquals("SAP", gioNode6.getId());
            assertEquals("label", gioNode6.getData().getKey());
            assertEquals("SAP", gioNode6.getData().getValue());





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


    }

    @DisplayName("Testing to load file with GioStandardGMLService.result has exception.")
    @Nested
    class unSuccessfulTest {

    }


}
