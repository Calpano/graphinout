package com.graphinout.reader.cj;

import com.graphinout.base.CjDocument2CjStream;
import com.graphinout.base.CjStream2GioWriter;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Reads .cj files (connected JSON) into a {@link GioWriter}.
 */
public class ConnectedJsonReader implements GioReader {

    public static final String FORMAT_ID = "connected-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Connected JSON Format", //
            ".con.json", ".connected.json", ".cj.json");
    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument readToDocument(String json) {
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
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
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        // we won't build a CJ to GIO Reader
        // so we reuse the path CJ -> GraphML -> GIO, until Graphinout replaced GIO with CJ

        // JSON -> CJ doc
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        try {
            JsonReaderImpl jsonReader = new JsonReaderImpl();
            jsonReader.read(inputSource, jsonWriter_in);
            ICjDocument cjDoc = cj2ElementsWriter.resultDoc();
            if (cjDoc == null) {
                cjDoc = new CjDocumentElement();
            }
            // CJ doc -> GIO
            CjStream2GioWriter cjStream2GioWriter = new CjStream2GioWriter(writer);
            CjDocument2CjStream.toCjStream(cjDoc, cjStream2GioWriter);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON content", e);
        }
    }

}
