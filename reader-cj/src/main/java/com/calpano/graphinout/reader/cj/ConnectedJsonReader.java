package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.reader.cj.json.JsonReaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public class ConnectedJsonReader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("connected-json", "Connected JSON Format", //
            ".con.json", ".connected.json");
    private static final Logger log = LoggerFactory.getLogger(Json5Reader.class);
    private @Nullable Consumer<ContentError> errorHandler;

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
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        Cj2GioWriter cj2Gio = new Cj2GioWriter(writer);
        Json2CjWriter json2Cj = new Json2CjWriter(cj2Gio);
        try {
            jsonReader.read(inputSource, json2Cj);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON content", e);
        }
    }

}
