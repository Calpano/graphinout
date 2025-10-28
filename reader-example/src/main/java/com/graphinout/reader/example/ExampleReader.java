package com.graphinout.reader.example;

import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.reader.Location;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class ExampleReader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("example", "Example Graph Format", ".example");
    private static final Logger log = getLogger(ExampleReader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        // here we read the inputStream of sis ...

        // and write the graph to our GioWriter
        ICjDocumentChunkMutable doc = writer.createDocumentChunk();
        writer.documentStart(doc);

        ICjGraphChunkMutable graph = writer.createGraphChunk();
        writer.graphStart(graph);

        ICjNodeChunkMutable node = writer.createNodeChunk();
        node.id("myNode1");
        writer.node(node);

        ICjNodeChunkMutable node2 = writer.createNodeChunk();
        node2.id("myNode2");
        writer.node(node2);

        ICjEdgeChunkMutable edge = writer.createEdgeChunk();
        edge.addEndpoint(ep -> ep.node("myNode1"));
        edge.addEndpoint(ep -> ep.node("myNode2"));
        writer.edge(edge);

        // content errors can be signaled like this
        errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, " To be honest, we did not really read the input :-)", new Location(1, 1)));

        writer.graphEnd();
        writer.documentEnd();
    }

}
