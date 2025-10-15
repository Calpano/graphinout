package com.graphinout.base.gio;

import com.graphinout.base.ReaderTests;
import com.graphinout.foundation.output.InMemoryOutputSink;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.xml.XmlFragmentString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class SyntheticGio {

    private static final Logger log = getLogger(SyntheticGio.class);

    public static void writeSmallDocument(GioWriter gioWriter) throws IOException {
        gioWriter.startDocument(GioDocument.builder().description(XmlFragmentString.ofPlainText("the doc")).build());
        gioWriter.startGraph(GioGraph.builder().edgedefaultDirected(true).build());
        gioWriter.startNode(GioNode.builder().id("node1").build());
        gioWriter.endNode(null);
        gioWriter.startNode(GioNode.builder().id("node2").build());
        gioWriter.endNode(null);
        gioWriter.startEdge(GioEdge.builder() //
                .endpoint(GioEndpoint.builder().node("node1").build())//
                .endpoint(GioEndpoint.builder().node("node2").build())//
                .build());
        gioWriter.endEdge();
        gioWriter.endGraph(null);
        gioWriter.endDocument();
    }

    @Test
    void test() throws IOException {
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GioWriter gioWriter = ReaderTests.createWriter(outputSink, true, true, true);
        writeSmallDocument(gioWriter);
        String graphml = outputSink.getBufferAsUtf8String();
        log.info("GraphML:\n" + graphml);
    }


}
