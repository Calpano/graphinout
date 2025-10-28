package com.graphinout.reader.dot;

import com.graphinout.base.GioReader;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.CjWriter2CjStream;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.validation.CjValidator;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.text.TextReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static org.slf4j.LoggerFactory.getLogger;

public class DotReader implements GioReader {

    public static final String FORMAT_ID = "dot";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "DOT Text Format", ".dot", ".gv", ".dot.txt", ".gv.txt");
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
        if (inputSource.isMulti())
            throw new IllegalArgumentException("Cannot handle multi-sources");
        SingleInputSource singleInputSource = inputSource.asSingle();
        String content = singleInputSource.getContentAsUtf8String();
        DotLines2CjDocument dotLines2CjDocument = new DotLines2CjDocument(errorHandler);
        TextReader.read(content, dotLines2CjDocument);
        CjDocumentElement cjDoc = dotLines2CjDocument.resultDocument();
        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(cjStream);
        cjDoc.fire(cjWriter2CjStream);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
