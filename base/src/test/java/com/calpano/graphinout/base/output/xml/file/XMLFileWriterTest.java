package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.GioGraphInOutXMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLFileWriterTest {

    private final static String FILE_NAME="test_graph_output.xml";
    @BeforeEach
    public  void setUp() throws IOException {

        Arrays.stream(new File("./").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(FILE_NAME);
            }
        })).sequential().forEach(File::deleteOnExit);

    }
    @Test
    void writeData_test() throws  IOException {

        Map<String,String> testKeyMap= new HashMap<>();
        testKeyMap.put("attr.name","type");
        testKeyMap.put("attr.type","string");
        testKeyMap.put("id","type");
        Map<String,String> testGraphMap= new HashMap<>();
        testGraphMap.put("edgedefault","directed");
        testGraphMap.put("id","");

        Map<String,String> testNode1Map= new HashMap<>();
        testNode1Map.put("testNode1Map","kb");
        testNode1Map.put("testNode1Map2","10");

        Map<String,String> testNode2Map= new HashMap<>();
        testNode2Map.put("testNode2Map","kb");
        testNode2Map.put("testNode2Map2","10");
        Map<String,String> testEdgeMap= new HashMap<>();
        testEdgeMap.put("ka","kb");
        testEdgeMap.put("kc","10");

        File file = new File("./"+FILE_NAME);
        XMLFileWriter instance = new XMLFileWriter(new OutputSinkMock(file));
        instance.startDocument();


        instance.startElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);
        instance.startElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME,testGraphMap);

        instance.startElement(GioGraphInOutXMLConstants.NODE_ELEMENT_NAME,testNode1Map);
        instance.endElement(GioGraphInOutXMLConstants.NODE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutXMLConstants.EDGE_ELEMENT_NAME,testEdgeMap);
        instance.endElement(GioGraphInOutXMLConstants.EDGE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutXMLConstants.NODE_ELEMENT_NAME,testNode2Map);
        instance.endElement(GioGraphInOutXMLConstants.NODE_ELEMENT_NAME);

        instance.startElement(GioGraphInOutXMLConstants.HYPER_EDGE_ELEMENT_NAME,testNode2Map);
        instance.startElement(GioGraphInOutXMLConstants.ENDPOINT_ELEMENT_NAME,testNode2Map);
        instance.endElement(GioGraphInOutXMLConstants.ENDPOINT_ELEMENT_NAME);
        instance.endElement(GioGraphInOutXMLConstants.HYPER_EDGE_ELEMENT_NAME);

        instance.endElement(GioGraphInOutXMLConstants.GRAPH_ELEMENT_NAME);
        instance.endElement(GioGraphInOutXMLConstants.GRAPHML_ELEMENT_NAME);

        instance.endDocument();
    }

    class OutputSinkMock implements  OutputSink{
        private final File  tmpFile ;
        private transient OutputStream out;
        private transient Writer w;

        public OutputSinkMock(File file)  {
            tmpFile =  file;
        }

        @Override
        public OutputStream outputStream() throws IOException {
            if(out==null)
                out = new FileOutputStream(tmpFile);
            return out;
        }

        @Override
        public List<String> readAllData() throws IOException {
            return Files.readAllLines(tmpFile.toPath());
        }

        @Override
        public Map<String, Object> outputInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("type","tmp file");
            info.put("name",tmpFile.getName());
            info.put("path",tmpFile.getAbsolutePath());
            return info;
        }

    }
}
