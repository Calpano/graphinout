package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.converter.standard.xml.StandardGraphML;
import com.calpano.graphinout.graph.GioEdge;
import com.calpano.graphinout.graph.GioEdgeData;
import com.calpano.graphinout.graph.GioGraph;
import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.graph.GioKey;
import com.calpano.graphinout.graph.GioNode;
import com.calpano.graphinout.graph.GioNodeData;


import com.calpano.graphinout.graphml.GraphMLService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author rbaba
 */
public class SGMLConverterTest {

    private final static StandardGraphML DEFUALT_GIO_GRAPH = createDefualtGioGraphML();

    public SGMLConverterTest() {
    }

    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Test of convert method, of class SGMLConverter.
     */
    @org.junit.jupiter.api.Test
    public void testConvert() throws Exception {

        System.out.println("convert");
        String xmlFile = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml-ids.xml";

        GraphMLService<StandardGraphML> xmlType = new GioStandardGMLService();
        SGMLConverter instance = new SGMLConverter();
        StandardGraphML expResult = null;
        StandardGraphML result = instance.convert(new File(xmlFile), xmlType);
        assertEquals(expResult, result);

    }

    @Test
    public void testMarshall() throws Exception {
        System.out.println("marshall");
        String fileName = Paths.get("src", "test", "resources", "graphin", "graphml", "test").toString() + "/graphml.xml";

        StandardGraphML graphML = DEFUALT_GIO_GRAPH;
       
        marshall(new File(fileName), graphML);

    }

    private static StandardGraphML createDefualtGioGraphML() {

        GioKey gioKey1 = new GioKey("test1", "test1", "test1", "default");
        GioKey gioKey2 = new GioKey("test2", "test2", "test2", "");
        GioKey gioKey3 = new GioKey("test3", "test3", "test3", null);

        List<GioKey> gioKeys = new ArrayList<>();
        gioKeys.add(gioKey1);
        gioKeys.add(gioKey2);
        gioKeys.add(gioKey3);
        StandardGraphML temp = new StandardGraphML();
        temp.setKeys(gioKeys);
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
//
    
    public void marshall(File file,StandardGraphML graphML){
         try {
            JAXBContext jc = JAXBContext.newInstance(GioGraphML.class);

            Marshaller u = jc.createMarshaller();
            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            u.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
            u.marshal(graphML, file);
        } catch (JAXBException ex) {
          ex.printStackTrace();
           
        }

    }

}
