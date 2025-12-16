package com.graphinout.reader.ocif.todo;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentError.ErrorLevel;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.todo.OcifJson2CjStream;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OcifReaderOLD implements GioReader {

    public static final String FORMAT_ID = "ocif";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "OCIF Open Canvas Interchange Format (OCIF v0.6)", ".ocif.json", ".ocif");
    private static final Logger log = LoggerFactory.getLogger(OcifReaderOLD.class);

    private @Nullable Consumer<ContentError> errorHandler;

    public static void parseOcifJsonString2CjStream(String json, ICjStream cjStream, @Nullable Consumer<ContentError> errorHandler) throws IOException {
        // Parse OCIF JSON and emit CJ stream events
        IJsonValue root = JavaJsons.ofJsonString(json);
        IJsonObject o = root == null ? null : root.asObject();
        if (o == null) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ErrorLevel.Error, "Invalid OCIF: root must be a JSON object", null));
            }
            throw new IOException("Invalid OCIF: Root element must be a JSON object");
        }
        OcifJson2CjStream.parseOcifJsonObject2CjStream(o, cjStream, errorHandler);

    }

    public static void readOcif(String inputName, String ocifJson, ICjStream cjStream) throws IOException {
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(inputName, ocifJson);
        OcifReaderOLD ocifReader = new OcifReaderOLD();
        ocifReader.read(inputSource, cjStream);
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        if (!(inputSource instanceof SingleInputSource sis)) {
            throw new IllegalArgumentException("Expected SingleInputSource");
        }

        String json;
        try (sis) {
            json = IOUtils.toString(sis.inputStream(), UTF_8);
        }

        parseOcifJsonString2CjStream(json, cjStream, errorHandler);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
