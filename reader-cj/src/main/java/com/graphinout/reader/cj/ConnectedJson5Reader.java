package com.graphinout.reader.cj;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json5.Json5Preprocessor;
import com.graphinout.foundation.json5.Json5Reader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.graphinout.foundation.input.SingleInputSourceOfString.inputSource;

/**
 * Buffers all content in memory first.
 */
public class ConnectedJson5Reader implements GioReader {

    public static final String FORMAT_ID = "connected-json5";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Connected JSON5 Format", //
            ".cj.json5", ".con.json5", ".connected.json5");
    private static final Logger log = LoggerFactory.getLogger(Json5Reader.class);
    private final ConnectedJsonReader cjReader = new ConnectedJsonReader();


    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.cjReader.setContentErrorHandler(errorHandler);
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
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
