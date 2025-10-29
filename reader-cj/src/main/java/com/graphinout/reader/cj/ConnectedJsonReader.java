package com.graphinout.reader.cj;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.writer.JsonWriter;
import com.graphinout.foundation.json.JsonReaderImpl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Reads .cj files (connected JSON) into a {@link ICjStream}.
 */
public class ConnectedJsonReader implements GioReader {

    public static final String FORMAT_ID = "connected-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Connected JSON Format", //
            ".cj.json", ".con.json", ".connected.json");
    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument readToDocument(String json) {
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        try {
            JsonReaderImpl jsonReader = new JsonReaderImpl();
            SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);
            jsonReader.read(inputSource, jsonWriter);
            return cj2ElementsWriter.resultDoc();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON content", e);
        }
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(writer);
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cjWriter2CjStream);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
