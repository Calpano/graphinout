package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;

import java.io.IOException;
import java.util.function.Consumer;

public class GraphmlReader implements GioReader {
    @Override
    public void errorHandler(Consumer<ContentError> errorConsumer) {
        GioReader.super.errorHandler(errorConsumer);
    }

    @Override
    public GioFileFormat fileFormat() {
        return null;
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {

    }
}
