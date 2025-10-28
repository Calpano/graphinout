package com.graphinout.base.cj;

import com.graphinout.base.ReaderTests;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.output.InMemoryOutputSink;
import com.graphinout.foundation.output.OutputSink;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class SyntheticCjTest {

    private static final Logger log = getLogger(SyntheticCjTest.class);

    public static void writeSmallDocument(ICjStream cjStream) {
        ICjDocumentChunkMutable doc = cjStream.createDocumentChunk();
        doc.descriptionPlainText(cjStream.jsonFactory(), "the doc");
        cjStream.documentStart(doc);

        ICjGraphChunk graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        cjStream.node(cjStream.createNodeChunkWithId("node1"));
        cjStream.node(cjStream.createNodeChunkWithId("node2"));

        ICjEdgeChunkMutable edge = cjStream.createEdgeChunk();
        edge.addEndpoint(ep -> ep.node("node1"));
        edge.addEndpoint(ep -> ep.node("node2"));
        cjStream.edge(edge);

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    @Test
    void test() throws IOException {
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        ICjStream gioWriter = ReaderTests.createCjStream(outputSink, true, true);
        writeSmallDocument(gioWriter);
        String graphml = outputSink.getBufferAsUtf8String();
        log.info("GraphML:\n" + graphml);
    }


}
