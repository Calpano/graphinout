package com.graphinout.reader.gexf;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;

import java.io.IOException;
import java.util.function.Consumer;

public class GexfReader implements GioReader {

    public static final String FORMAT_ID = "gexf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GEXF Format", ".gexf", ".gexf.xml");
    private Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        // TODO use XML reader here, like in Graphml
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
