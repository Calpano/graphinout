package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.reader.cj.json.Json5Preprocessor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.input.SingleInputSourceOfString.inputSource;

/**
 * Buffers all content in memory first.
 */
public class ConnectedJson5Reader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("connected-json5", "Connected JSON5 Format", //
            ".con.json5", ".connected.json5");
    private static final Logger log = LoggerFactory.getLogger(Json5Reader.class);
    private final ConnectedJsonReader cjReader = new ConnectedJsonReader();

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.cjReader.errorHandler(errorHandler);
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }

        // strip
        String json = Json5Preprocessor.toJson(content);
        // construct new input source
        SingleInputSourceOfString strippedInput = inputSource(inputSource.name() + "-preprocessed", json);
        // parse normal
        cjReader.read(strippedInput, writer);
    }


}
