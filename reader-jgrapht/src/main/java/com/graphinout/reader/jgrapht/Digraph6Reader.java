package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static org.slf4j.LoggerFactory.getLogger;

public class Digraph6Reader implements GioReader {

    private static final Logger log = getLogger(Digraph6Reader.class);
    public static final String FORMAT_ID = "digraph6";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "digraph6 format", ".d6", ".digraph6");
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
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        ifPresentAccept(errorHandler, cjStream::setContentErrorHandler);
        JGraphTReader<Integer> jGraphTReader = new JGraphTReader<>(inputSource, Digraph6EventDrivenImporter::new, cjStream, Object::toString);
        jGraphTReader.read();
    }

}
