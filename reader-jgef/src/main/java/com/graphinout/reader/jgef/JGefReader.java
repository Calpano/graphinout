package com.graphinout.reader.jgef;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.cj.ConnectedJsonReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Reads the JSON Graph Entry Format (GEF) — a lenient superset of Connected JSON. It normalizes GEF to canonical CJ
 * (see {@link Gef}) and delegates parsing to {@link ConnectedJsonReader}, the same way {@code ConnectedJson5Reader}
 * delegates after a JSON5 → JSON preprocess. Buffers the whole input in memory.
 */
public class JGefReader implements GioReader {

    public static final String FORMAT_ID = "json-gef";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "JSON Graph Entry Format", //
            ".gef.json", ".gef", ".jgef");

    private final ConnectedJsonReader cjReader = new ConnectedJsonReader();

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        cjReader.setContentErrorHandler(errorHandler);
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return;
        }
        String cjJson = Gef.toConnectedJson(content);
        cjReader.read(SingleInputSourceOfString.of(inputSource.name() + "-gef2cj", cjJson), writer);
    }
}
