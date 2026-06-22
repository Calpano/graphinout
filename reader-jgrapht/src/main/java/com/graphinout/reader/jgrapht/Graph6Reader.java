package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.base.input.InputSource;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * Reads the nauty <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">graph6</a> format: simple,
 * undirected, unlabelled graphs. Each line is one graph; vertices map to positional CJ node ids
 * {@code "0".."n-1"} and edges are undirected. Decoding is done by {@link Graph6Codec}.
 */
public class Graph6Reader implements GioReader {

    public static final String FORMAT_ID = "graph6";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "graph6 format", ".g6", ".graph6");
    private Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        ifPresentAccept(errorHandler, cjStream::setContentErrorHandler);
        Graph6Emitter.read(inputSource, cjStream, false, Graph6Codec::decodeGraph6);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
