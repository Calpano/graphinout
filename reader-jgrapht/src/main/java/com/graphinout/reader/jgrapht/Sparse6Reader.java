package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.GioReader;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import org.jgrapht.nio.graph6.Graph6Sparse6EventDrivenImporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static org.slf4j.LoggerFactory.getLogger;

public class Sparse6Reader implements GioReader {

    public static final String FORMAT_ID = "sparse6";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "sparse6 format", ".s6", ".sparse6");
    private static final Logger log = getLogger(Sparse6Reader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        ifPresentAccept(errorHandler, cjStream::setContentErrorHandler);
        JGraphTReader<Integer> jGraphTReader = new JGraphTReader<>(inputSource, Graph6Sparse6EventDrivenImporter::new, cjStream, Object::toString);
        jGraphTReader.read();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
