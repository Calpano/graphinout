package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.json.JsonEventStream;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.reader.cj.json.JsonReaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

public class ConnectedJsonReader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("connected-json", "Connected JSON Format", //
            ".con.json", ".connected.json");
    private static final Logger log = LoggerFactory.getLogger(Json5Reader.class);
    private final JsonEventStream jsonHandler = new JsonEventStream() {
        @Override
        public void arrayEnd() throws JsonException {

        }

        @Override
        public void arrayStart() throws JsonException {

        }

        @Override
        public void documentEnd() {

        }

        @Override
        public void documentStart() {

        }

        @Override
        public void objectEnd() throws JsonException {

        }

        @Override
        public void objectStart() throws JsonException {

        }

        @Override
        public void onBigDecimal(BigDecimal bigDecimal) {

        }

        @Override
        public void onBigInteger(BigInteger bigInteger) {

        }

        @Override
        public void onBoolean(boolean b) throws JsonException {

        }

        @Override
        public void onDouble(double d) throws JsonException {

        }

        @Override
        public void onFloat(float f) throws JsonException {

        }

        @Override
        public void onInteger(int i) throws JsonException {

        }

        @Override
        public void onKey(String key) throws JsonException {

        }

        @Override
        public void onLong(long l) throws JsonException {

        }

        @Override
        public void onNull() throws JsonException {

        }

        @Override
        public void onString(String s) throws JsonException {

        }
    };
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
        Cj2Gio cj2Gio = new Cj2Gio(writer);
        Json2Cj json2Cj = new Json2Cj(cj2Gio);
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
