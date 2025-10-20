package com.graphinout.reader.jgrapht;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import org.jgrapht.nio.graph6.Graph6Sparse6EventDrivenImporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class Sparse6Reader implements GioReader {

    private static final Logger log = getLogger(Sparse6Reader.class);
    public static final String FORMAT_ID = "sparse6";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "sparse6 format", ".s6", ".sparse6");
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        JGraphTReader<Integer> jGraphTReader = new JGraphTReader<>(inputSource, Graph6Sparse6EventDrivenImporter::new, writer, errorHandler, Object::toString);
        jGraphTReader.read();
    }

}
