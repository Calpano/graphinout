package com.graphinout.reader.ocif07;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.annotations.quality.QualityOK;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.input.ContentErrors;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.cj.OcifDoc2CjDoc;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static java.nio.charset.StandardCharsets.UTF_8;

@QualityOK
public class OcifReader implements GioReader {

    public static final String FORMAT_ID = "ocif";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "OCIF Open Canvas Interchange Format (OCIF v0.6)", ".ocif.json", ".ocif");
    private static final Logger log = LoggerFactory.getLogger(OcifReader.class);

    private @NonNull Consumer<ContentError> errorHandler;

    public OcifReader() {errorHandler = ContentErrors.NOOP;}


    public static void parseOcifJsonString2CjStream(String json, ICjStream cjStream, @NonNull Consumer<ContentError> errorHandler) throws ContentErrorException {
        // parse string to JSON value
        IJsonValue root = JavaJsons.ofJsonString(json);
        if (root == null) {
            throw ContentErrorException.contentError("Invalid OCIF: root must be a JSON object");
        }
        // next, parse IJsonValue to OCIF document
        OcifDocument ocifDocument = Json2OcifDoc.toOcifDocument(root, errorHandler);
        // to CjDocument
        ICjDocument cjDocument = OcifDoc2CjDoc.toCjDocument(ocifDocument);
        // fire
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(cjStream);
        cjDocument.fire(cjWriter2CjStream, false);
    }


    /**
     * @param inputName
     * @param ocifJson
     * @param cjStream
     * @param errorHandler if not null, is used instead of current (usually: NOOP) error handler
     * @throws IOException
     */
    public static void readOcif(String inputName, String ocifJson, ICjStream cjStream, @Nullable Consumer<ContentError> errorHandler) throws IOException {
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(inputName, ocifJson);
        OcifReader ocifReader = new OcifReader();
        ifPresentAccept(errorHandler, ocifReader::setContentErrorHandler);
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
