package com.calpano.graphinout.reader.jgrapht.dot;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.reader.jgrapht.JGraphTReader;
import org.jgrapht.nio.dot.DOTEventDrivenImporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class DotReader implements GioReader {

    private static final Logger log = getLogger(DotReader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot", "DOT Text Format", ".dot", ".gv");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        JGraphTReader<String> jGraphTReader = new JGraphTReader<>(inputSource, DOTEventDrivenImporter::new, writer, errorHandler, vertex -> vertex);
        jGraphTReader.read();
    }
}
