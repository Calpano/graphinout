package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.base.GioGraphInOutXMLConstants;
import com.calpano.graphinout.foundation.output.FileOutputSink;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

class XmlWriterTest {


    private final static String FILE_NAME = "test_graph_output.xml";

    @BeforeEach
    public void setUp() throws IOException {

        Arrays.stream(new File("./target/").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(FILE_NAME);
            }
        })).sequential().forEach(File::deleteOnExit);

    }

    @Test
    void test() throws IOException {
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        XmlWriterImpl xmlWriter = new XmlWriterImpl(sink);
        xmlWriter.startDocument();
        xmlWriter.startElement("test");
        xmlWriter.characterData("test");
        xmlWriter.endElement("test");
        xmlWriter.endDocument();
        String s = sink.getBufferAsUtf8String();
        assertThat(s).isEqualTo("<test>test</test>\n");
    }

    @Test
    void writeData_test() throws IOException {

        Map<String, String> testKeyMap = new HashMap<>();
        testKeyMap.put("attr.name", "type");
        testKeyMap.put("attr.type", "string");
        testKeyMap.put("id", "type");
        Map<String, String> testGraphMap = new HashMap<>();
        testGraphMap.put("edgedefault", "directed");
        testGraphMap.put("id", "");

        Map<String, String> testNode1Map = new HashMap<>();
        testNode1Map.put("testNode1Map", "kb");
        testNode1Map.put("testNode1Map2", "10");

        Map<String, String> testNode2Map = new HashMap<>();
        testNode2Map.put("testNode2Map", "kb");
        testNode2Map.put("testNode2Map2", "10");
        Map<String, String> testEdgeMap = new HashMap<>();
        testEdgeMap.put("ka", "kb");
        testEdgeMap.put("kc", "10");

        File file = new File("./target/" + FILE_NAME);
        XmlWriterImpl instance = new XmlWriterImpl(new FileOutputSink(file));
        instance.startDocument();


        instance.startElement(GioGraphInOutXMLConstants.GRAPHML);
        instance.startElement(GioGraphInOutXMLConstants.GRAPH, testGraphMap);

        instance.startElement(GioGraphInOutXMLConstants.NODE, testNode1Map);
        instance.endElement(GioGraphInOutXMLConstants.NODE);

        instance.startElement(GioGraphInOutXMLConstants.EDGE, testEdgeMap);
        instance.endElement(GioGraphInOutXMLConstants.EDGE);

        instance.startElement(GioGraphInOutXMLConstants.NODE, testNode2Map);
        instance.endElement(GioGraphInOutXMLConstants.NODE);

        instance.startElement(GioGraphInOutXMLConstants.HYPER_EDGE, testNode2Map);
        instance.startElement(GioGraphInOutXMLConstants.ENDPOINT, testNode2Map);
        instance.endElement(GioGraphInOutXMLConstants.ENDPOINT);
        instance.endElement(GioGraphInOutXMLConstants.HYPER_EDGE);

        instance.endElement(GioGraphInOutXMLConstants.GRAPH);
        instance.endElement(GioGraphInOutXMLConstants.GRAPHML);

        instance.endDocument();
    }

}
