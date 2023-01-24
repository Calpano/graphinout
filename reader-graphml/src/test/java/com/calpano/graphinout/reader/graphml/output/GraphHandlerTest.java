package com.calpano.graphinout.reader.graphml.output;

import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.output.OutputHandler;
import com.calpano.graphinout.base.output.GraphHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

class GraphHandlerTest {

    private final static String FILE_NAME = "test_graph_output.xml";

    @BeforeEach
    public void setUp() throws IOException {

        Arrays.stream(new File("./").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(FILE_NAME);
            }
        })).sequential().forEach(File::deleteOnExit);

    }

    @Test
    void initialize() throws GioException {

        LinkedHashMap<String, String> testKeyMap = new LinkedHashMap<>();
        testKeyMap.put("attr.name", "type");
        testKeyMap.put("attr.type", "string");
        testKeyMap.put("id", "type");
        LinkedHashMap<String, String> testGraphMap = new LinkedHashMap<>();
        testGraphMap.put("edgedefault", "directed");
        testGraphMap.put("id", "");

        LinkedHashMap<String, String> testNode1Map = new LinkedHashMap<>();
        testNode1Map.put("testNode1Map", "kb");
        testNode1Map.put("testNode1Map2", "10");

        LinkedHashMap<String, String> testNode2Map = new LinkedHashMap<>();
        testNode2Map.put("testNode2Map", "kb");
        testNode2Map.put("testNode2Map2", "10");
        LinkedHashMap<String, String> testEdgeMap = new LinkedHashMap<>();
        testEdgeMap.put("ka", "kb");
        testEdgeMap.put("kc", "10");

        File file = new File("./" + FILE_NAME);
        OutputHandler<File> instance = new GraphHandler();
        instance.initialize(file);


        instance.startElement(GioGraphInOutConstants.GRAPH_ELEMENT_NAME, testGraphMap);

        instance.startElement(GioGraphInOutConstants.NODE_ELEMENT_NAME, testNode1Map);
        instance.endElement(GioGraphInOutConstants.NODE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutConstants.EDGE_ELEMENT_NAME, testEdgeMap);
        instance.endElement(GioGraphInOutConstants.EDGE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutConstants.NODE_ELEMENT_NAME, testNode2Map);
        instance.endElement(GioGraphInOutConstants.NODE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME, testNode2Map);
        instance.startElement(GioGraphInOutConstants.ENDPOINT_ELEMENT_NAME, testNode2Map);
        instance.endElement(GioGraphInOutConstants.ENDPOINT_ELEMENT_NAME);
        instance.endElement(GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME);

        instance.endElement(GioGraphInOutConstants.GRAPH_ELEMENT_NAME);

    }
}