package com.calpano.graphinout.reader.example;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class ExampleReader implements GioReader {

    private static final Logger log = getLogger(ExampleReader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("example", "Example Graph Format", ".example");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        // here we read the inputStream of sis ...

        // and write the graph to our GioWriter
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        writer.startNode(GioNode.builder().id("myNode1").build());
        writer.startNode(GioNode.builder().id("myNode2").build());
        writer.startEdge(GioEdge.builder().endpoints(Arrays.asList(GioEndpoint.builder().id("myNode1").build(), GioEndpoint.builder().id("myNode2").build())).build());

        // content errors can be signaled like this
        errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, " To be honest, we did not really read the input :-)", new ContentError.Location(1, 1)));

        writer.endGraph(null);
        writer.endDocument();
    }
}
