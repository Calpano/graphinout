package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.foundation.output.FileOutputSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

class XmlWriterTest {

    private final static String FILE_NAME = "test_graph_output.xml";

    @BeforeEach
    public void setUp() throws IOException {
        File targetDir = new File("./target/");
        Arrays.stream(requireNonNull(targetDir.listFiles(pathname -> pathname.getName().startsWith(FILE_NAME)))).sequential().forEach(File::deleteOnExit);
    }

    @Test
    void test() throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        xmlWriter.documentStart();
        xmlWriter.elementStart("test");
        xmlWriter.characterDataStart(false);
        xmlWriter.characterData("test", false);
        xmlWriter.characterDataEnd(false);
        xmlWriter.elementEnd("test");
        xmlWriter.documentEnd();
        String s = xmlWriter.string();
        assertThat(s).isEqualTo(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + "<test>test</test>");
    }

    @Test
    void testCdata() throws IOException {
        // a test XML string with CDATA sections
        String chars = "AAA <![CDATA[BBB]]>CCC<![CDATA[DDD < & > EEE]]>FFF";
        String xml = "<root>" + chars + "</root>";
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        xmlWriter.documentStart();
        xmlWriter.elementStart("root");
        xmlWriter.characterDataWhichMayContainCdata(chars);
        xmlWriter.elementEnd("root");
        xmlWriter.documentEnd();

        String out = xmlWriter.string();
        assertThat(out).isEqualTo(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + xml);
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
        XmlWriterImpl instance = XmlWriterImpl.create(new FileOutputSink(file));
        instance.documentStart();


        instance.elementStart(GraphmlElements.GRAPHML);
        instance.elementStart(GraphmlElements.GRAPH, testGraphMap);

        instance.elementStart(GraphmlElements.NODE, testNode1Map);
        instance.elementEnd(GraphmlElements.NODE);

        instance.elementStart(GraphmlElements.EDGE, testEdgeMap);
        instance.elementEnd(GraphmlElements.EDGE);

        instance.elementStart(GraphmlElements.NODE, testNode2Map);
        instance.elementEnd(GraphmlElements.NODE);

        instance.elementStart(GraphmlElements.HYPER_EDGE, testNode2Map);
        instance.elementStart(GraphmlElements.ENDPOINT, testNode2Map);
        instance.elementEnd(GraphmlElements.ENDPOINT);
        instance.elementEnd(GraphmlElements.HYPER_EDGE);

        instance.elementEnd(GraphmlElements.GRAPH);
        instance.elementEnd(GraphmlElements.GRAPHML);

        instance.documentEnd();
    }

}
