package com.graphinout.reader.jgrapht.dot;

import com.graphinout.base.GioReader;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.validation.CjValidator;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.reader.jgrapht.JGraphTReader;
import org.jgrapht.nio.dot.DOTEventDrivenImporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static org.slf4j.LoggerFactory.getLogger;

public class DotReader implements GioReader {

    public static final String FORMAT_ID = "dot";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "DOT Text Format", ".dot", ".gv");
    private static final Logger log = getLogger(DotReader.class);
    private Consumer<ContentError> errorHandler;

    public static String readToCjJson(InputSource inputSource) throws IOException {
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter);
        DotReader dotReader = new DotReader();
        dotReader.read(inputSource, cjStream2CjWriter);

        String json = json2StringWriter.jsonString();
        SingleInputSourceOfString jsonInputSource = new SingleInputSourceOfString("parsed", json);
        assert CjValidator.isValidCjCanonical(jsonInputSource);
        return json;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        ifPresentAccept(errorHandler, cjStream::setContentErrorHandler);
        JGraphTReader<String> jGraphTReader = new JGraphTReader<>(inputSource, DOTEventDrivenImporter::new, cjStream, v -> v);
        jGraphTReader.read();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
