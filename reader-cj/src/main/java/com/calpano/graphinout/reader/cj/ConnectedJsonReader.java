package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.reader.graphml.Graphml2GioWriter;
import com.calpano.graphinout.reader.graphml.cj.CjDocument2Graphml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Reads .cj files (connected JSON) into a {@link GioWriter}.
 */
public class ConnectedJsonReader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("connected-json", "Connected JSON Format", //
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
            // CJ doc -> GraphML -> GIO
            Graphml2GioWriter graphml2GioWriter = new Graphml2GioWriter(writer);
            CjDocument2Graphml cjDocument2Graphml = new CjDocument2Graphml(graphml2GioWriter);
            cjDocument2Graphml.writeDocumentToGraphml(cjDoc);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON content", e);
        }
    }

}
