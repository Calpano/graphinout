package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.Direction;
import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import com.calpano.graphinout.base.graphml.GraphMLService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

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

            GraphMLService<GioDocument> myGMLService = new GioStandardGMLService();
            SGMLConverter instance = new SGMLConverter();

            GioDocument result = instance.convert(new File(xmlFile), new File("./output.xml"), myGMLService);
            assertNull(result.getId());
            assertNull(result.getKeys());
            assertEquals(1, result.getGraphs().size());

            GioGraph gioGraph = result.getGraphs().get(0);
            assertEquals("id--graph", gioGraph.getId());
            assertEquals(Direction.Directed, gioGraph.getEdgedefault());

            List<GioNode> gioNodes = gioGraph.getNodes();
            assertEquals(6, gioNodes.size());

            GioNode gioNode1 = gioNodes.get(0);
            assertEquals("id--node1", gioNode1.getId());
            assertNull(gioNode1.getDataList());
            assertEquals(0, gioNode1.getPorts().size());

            GioNode gioNode2 = gioNodes.get(1);
            assertEquals("id--node2", gioNode2.getId());
            assertNull(gioNode2.getDataList());
            assertEquals(0, gioNode2.getPorts().size());

            GioNode gioNode3 = gioNodes.get(2);
            assertEquals("id--node3", gioNode3.getId());
            assertNull(gioNode3.getDataList());
            assertEquals(0, gioNode3.getPorts().size());


            GioNode gioNode4 = gioNodes.get(3);
            assertEquals("id--node4", gioNode4.getId());
            assertNull(gioNode4.getDataList());
            assertEquals(2, gioNode4.getPorts().size());

            GioPort gioPort41 = gioNode4.getPorts().get(0);
            assertEquals("North", gioPort41.getName());

            GioPort gioPort42 = gioNode4.getPorts().get(1);
            assertEquals("South", gioPort42.getName());

            GioNode gioNode5 = gioNodes.get(4);
            assertEquals("id--node5", gioNode5.getId());
            assertNull(gioNode5.getDataList());
            assertEquals(2, gioNode5.getPorts().size());

            GioPort gioPort51 = gioNode5.getPorts().get(0);
            assertEquals("North", gioPort51.getName());

            GioPort gioPort52 = gioNode5.getPorts().get(1);
            assertEquals("South", gioPort52.getName());

            GioNode gioNode6 = gioNodes.get(5);
            assertEquals("id--node6", gioNode6.getId());
            assertNull(gioNode6.getDataList());
            assertEquals(2, gioNode6.getPorts().size());

            GioPort gioPort61 = gioNode6.getPorts().get(0);
            assertEquals("North", gioPort61.getName());

            GioPort gioPort62 = gioNode6.getPorts().get(1);
            assertEquals("South", gioPort62.getName());


            List<GioEdge> hyperEdges = gioGraph.getHyperEdges();
            assertEquals(4, hyperEdges.size());

            GioEdge gioHyperEdge1 = hyperEdges.get(0);
            assertNull(gioHyperEdge1.getDataList());
            assertEquals("id--edge-12", gioHyperEdge1.getId());
            List<GioEndpoint> gioEndpoints = gioHyperEdge1.getEndpoints();
            assertEquals(2, gioEndpoints.size());
            GioEndpoint gioEndpoint1 = gioEndpoints.get(0);
            assertEquals(gioNode1.getId(), gioEndpoint1.getNode());
            assertEquals(Direction.In, gioEndpoint1.getType());
            GioEndpoint gioEndpoint2 = gioEndpoints.get(1);
            assertEquals(gioNode2.getId(), gioEndpoint2.getNode());
            assertEquals(Direction.Out, gioEndpoint2.getType());


            GioEdge gioHyperEdge2 = hyperEdges.get(1);
            assertNull(gioHyperEdge2.getDataList());
            assertEquals("id--edge-4S6N", gioHyperEdge2.getId());
            List<GioEndpoint> gioEndpoints2 = gioHyperEdge2.getEndpoints();
            assertEquals(2, gioEndpoints2.size());
            GioEndpoint gioEndpoint21 = gioEndpoints2.get(0);
            assertEquals(gioNode4.getId(), gioEndpoint21.getNode());
            assertEquals("South", gioEndpoint21.getPort());
            assertEquals(Direction.In, gioEndpoint1.getType());
            GioEndpoint gioEndpoint22 = gioEndpoints2.get(1);
            assertEquals(gioNode6.getId(), gioEndpoint22.getNode());
            assertEquals("North", gioEndpoint22.getPort());
            assertEquals(Direction.Out, gioEndpoint22.getType());


            GioEdge gioHyperEdge3 = hyperEdges.get(2);
            assertEquals("id--hyperedge-123", gioHyperEdge3.getId());

            List<GioEndpoint> gioEndpoints31 = gioHyperEdge3.getEndpoints();
            assertEquals(3, gioEndpoints31.size());

            GioEndpoint gioEndpoint111 = gioEndpoints31.get(0);
            assertNull(gioEndpoint111.getId());
            assertEquals(Direction.In, gioEndpoint111.getType());
            assertEquals(gioNode1.getId(), gioEndpoint111.getNode());
            assertNull(gioEndpoint111.getPort());

            GioEndpoint gioEndpoint112 = gioEndpoints31.get(1);
            assertNull(gioEndpoint112.getId());
            assertEquals(Direction.Out, gioEndpoint112.getType());
            assertEquals(gioNode2.getId(), gioEndpoint112.getNode());
            assertNull(gioEndpoint112.getPort());

            GioEndpoint gioEndpoint113 = gioEndpoints31.get(2);
            assertNull(gioEndpoint113.getId());
            assertEquals(Direction.Undirected, gioEndpoint113.getType());
            assertEquals(gioNode3.getId(), gioEndpoint113.getNode());
            assertNull(gioEndpoint113.getPort());

            GioEdge gioHyperEdge4 = hyperEdges.get(3);
            assertEquals("id--hyperedge-4N56", gioHyperEdge4.getId());

            List<GioEndpoint> gioEndpoints12 = gioHyperEdge4.getEndpoints();
            assertEquals(3, gioEndpoints12.size());

            GioEndpoint gioEndpoint121 = gioEndpoints12.get(0);
            assertNull(gioEndpoint121.getId());
            assertEquals(Direction.In, gioEndpoint121.getType());
            assertEquals(gioNode4.getId(), gioEndpoint121.getNode());
            assertEquals("North", gioEndpoint121.getPort());


            GioEndpoint gioEndpoint122 = gioEndpoints12.get(1);
            assertNull(gioEndpoint122.getId());
            assertEquals(Direction.In, gioEndpoint122.getType());
            assertEquals(gioNode5.getId(), gioEndpoint122.getNode());
            assertNull(gioEndpoint122.getPort());

            GioEndpoint gioEndpoint123 = gioEndpoints12.get(2);
            assertNull(gioEndpoint123.getId());
            assertEquals(Direction.In, gioEndpoint123.getType());
            assertEquals(gioNode6.getId(), gioEndpoint123.getNode());
            assertNull(gioEndpoint123.getPort());


        }

        @DisplayName("Load file graph-a.graphml")
        @Test
        @Disabled
        public void testConvert_graph_a_graphml() throws Exception {
            String xmlFile = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graph-a.graphml";

            GraphMLService<GioDocument> myGMLService = new GioStandardGMLService();
            SGMLConverter instance = new SGMLConverter();

            GioDocument result = instance.convert(new File(xmlFile), new File("./output.xml"), myGMLService);

            List<GioKey> keys = result.getKeys();
            assertEquals(1, keys.size());
            GioKey gioKey1 = keys.get(0);
            assertEquals("type", gioKey1.getId());
            assertEquals("type", gioKey1.getAttrName());
            assertEquals("string", gioKey1.getAttrType());
            assertNull(gioKey1.getDefaultValue());

            assertEquals(1, result.getGraphs().size());
            GioGraph gioGraph = result.getGraphs().get(0);

            assertEquals(Direction.Directed, gioGraph.getEdgedefault());
            assertEquals("", gioGraph.getId());


            List<GioNode> gioNodes = gioGraph.getNodes();
            assertEquals(7, gioNodes.size());


            GioNode gioNode0 = gioNodes.get(0);
            assertEquals("cat", gioNode0.getId());
            List<GioData> datas = gioNode0.getDataList();
            assertEquals(1, datas.size());
            GioData gioData1 = datas.get(0);

            assertEquals("label", gioData1.getKey());
            assertEquals("cat", gioData1.getValue());


            GioNode gioNode1 = gioNodes.get(1);
            assertEquals("ClaudiaStern", gioNode1.getId());
            List<GioData> datas1 = gioNode1.getDataList();
            assertEquals(1, datas1.size());
            GioData gioData11 = datas1.get(0);

            assertEquals("label", gioData11.getKey());
            assertEquals("Claudia Stern", gioData11.getValue());


            GioNode gioNode2 = gioNodes.get(2);
            assertEquals("ClaudiCat", gioNode2.getId());
            List<GioData> datas2 = gioNode2.getDataList();
            assertEquals(1, datas2.size());
            GioData gioData12 = datas2.get(0);
            assertEquals("label", gioData12.getKey());
            assertEquals("ClaudiCat", gioData12.getValue());

            GioNode gioNode3 = gioNodes.get(3);
            List<GioData> datas3 = gioNode3.getDataList();
            assertEquals(1, datas3.size());
            GioData gioData13 = datas3.get(0);
            assertEquals("DirkHagemann", gioNode3.getId());
            assertEquals("label", gioData13.getKey());
            assertEquals("Dirk Hagemann", gioData13.getValue());

            GioNode gioNode4 = gioNodes.get(4);
            List<GioData> datas4 = gioNode4.getDataList();
            assertEquals(1, datas4.size());
            GioData gioData14 = datas3.get(0);

            assertEquals("Company", gioNode4.getId());
            assertEquals("label", gioData14.getKey());
            assertEquals("Company", gioData14.getValue());


            GioNode gioNode5 = gioNodes.get(5);
            List<GioData> datas5 = gioNode5.getDataList();
            assertEquals(1, datas5.size());
            GioData gioData15 = datas5.get(0);
            assertEquals("Person", gioNode5.getId());
            assertEquals("label", gioData15.getKey());
            assertEquals("Person", gioData15.getValue());


            GioNode gioNode6 = gioNodes.get(6);
            List<GioData> datas6 = gioNode6.getDataList();
            assertEquals(1, datas6.size());
            GioData gioData16 = datas6.get(0);
            assertEquals("SAP", gioNode6.getId());
            assertEquals("label", gioData16.getKey());
            assertEquals("SAP", gioData16.getValue());


            List<GioEdge> gioHyperEdges = gioGraph.getHyperEdges();
            assertEquals(11, gioHyperEdges.size());
            GioEdge hyperEdge1 = gioHyperEdges.get(0);
            assertNull(hyperEdge1.getId());
            List<GioEndpoint> gioEndpoints1 = hyperEdge1.getEndpoints();
            assertEquals(2, gioEndpoints1.size());
            GioEndpoint gioEndpoint11 = gioEndpoints1.get(0);
            assertEquals("SAP", gioEndpoint11.getNode());
            assertEquals(Direction.In, gioEndpoint11.getType());

            GioEndpoint gioEndpoint12 = gioEndpoints1.get(1);
            assertEquals("Company", gioEndpoint12.getNode());
            assertEquals(Direction.Out, gioEndpoint12.getType());

            List<GioData> gioDatas1 = hyperEdge1.getDataList();
            assertEquals("1", gioDatas1.size());
            GioData data11 = gioDatas1.get(0);
            assertEquals("type", data11.getKey());
            assertEquals("hasType", data11.getValue());

        }


    }

    @DisplayName("Testing to load file with GioStandardGMLService.result has exception.")
    @Nested
    class unSuccessfulTest {

    }


}
