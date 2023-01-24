package com.calpano.graphinout.reader.graphml.output;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.output.OutputHandler;

import com.calpano.graphinout.base.output.file.ElementHandler;
import org.junit.jupiter.api.*;

/**
 *
 * @author rbaba
 */
public class DefaultFileOutputHandlerTest {
    
    public DefaultFileOutputHandlerTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }

    private final static String FILE_NAME="test_graphml_output.xml";
    @BeforeEach
    public  void setUp() throws IOException {

        Arrays.stream(new File("./").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(FILE_NAME);
            }
        })).sequential().forEach(File::deleteOnExit);

    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class DefaultFileOutputHandler.
     */
    @Test
    public void testInitialize() throws Exception {
         LinkedHashMap<String,String> testKeyMap= new  LinkedHashMap<>();
        testKeyMap.put("attr.name","type");
        testKeyMap.put("attr.type","string");
        testKeyMap.put("id","type");
         LinkedHashMap<String,String> testGraphMap= new  LinkedHashMap<>();
        testGraphMap.put("edgedefault","directed");
        testGraphMap.put("id","");

         LinkedHashMap<String,String> testNode1Map= new  LinkedHashMap<>();
        testNode1Map.put("testNode1Map","kb");
        testNode1Map.put("testNode1Map","10");

         LinkedHashMap<String,String> testNode2Map= new  LinkedHashMap<>();
        testNode2Map.put("testNode2Map","kb");
        testNode2Map.put("testNode2Map","10");
         LinkedHashMap<String,String> testEdgeMap= new  LinkedHashMap<>();
        testEdgeMap.put("ka","kb");
        testEdgeMap.put("kc","10");

        File file = new File("./"+FILE_NAME);
        OutputHandler instance = new ElementHandler();
        instance.initialize(file);
        instance.startElement(GioGraphInOutConstants.GRAPHML_ELEMENT_NAME);
        instance.startElement(GioGraphInOutConstants.KEY_ELEMENT_NAME,testKeyMap);
        instance.endElement(GioGraphInOutConstants.KEY_ELEMENT_NAME);


        instance.startElement(GioGraphInOutConstants.GRAPH_ELEMENT_NAME,testGraphMap);
        instance.startElement(GioGraphInOutConstants.NODE_ELEMENT_NAME,testNode1Map);
        instance.endElement(GioGraphInOutConstants.NODE_ELEMENT_NAME);
        instance.startElement(GioGraphInOutConstants.EDGE_ELEMENT_NAME,testEdgeMap);
        instance.endElement(GioGraphInOutConstants.EDGE_ELEMENT_NAME);
        instance.startElement(GioGraphInOutConstants.NODE_ELEMENT_NAME,testNode2Map);
        instance.endElement(GioGraphInOutConstants.NODE_ELEMENT_NAME);
        instance.endElement(GioGraphInOutConstants.GRAPH_ELEMENT_NAME);
        instance.endElement(GioGraphInOutConstants.GRAPHML_ELEMENT_NAME);



    }




    
}
