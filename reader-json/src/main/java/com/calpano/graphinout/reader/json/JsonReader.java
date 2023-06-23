package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.FilesMultiInputSource;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.MultiInputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;

import java.io.IOException;
import java.util.function.Consumer;

public class JsonReader implements GioReader {
    public static final String DATA = "data";
    public static final String MAPPING = "mapping";
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
        if(inputSource.isMulti()) {
            MultiInputSource multiInputSource = (MultiInputSource) inputSource;
            SingleInputSource dataSource = multiInputSource.getNamedSource(DATA);
            SingleInputSource mappingSource = multiInputSource.getNamedSource(MAPPING);
            // TODO read data and mapping
        } else {
            throw new IOException("JsonReader expects a multi-input for data and mapping");
        }
    }
}
