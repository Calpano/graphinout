package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;

import java.io.IOException;
import java.util.function.Consumer;

public class JsonReader implements GioReader {
    private Consumer<ContentError> errorHandler;
    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler=errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("json", "Json", ".json");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {

    }
}
