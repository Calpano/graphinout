package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.jgrapht.nio.graph6.Graph6Sparse6EventDrivenImporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class Graph6Reader implements GioReader {

    private static final Logger log = getLogger(Graph6Reader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("graph6", "graph6 format", ".g6", ".graph6");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        JGraphTReader<Integer> jGraphTReader = new JGraphTReader<>(inputSource, Graph6Sparse6EventDrivenImporter::new, writer, errorHandler, Object::toString);
        jGraphTReader.read();
    }
}
