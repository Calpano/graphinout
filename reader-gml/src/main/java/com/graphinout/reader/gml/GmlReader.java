package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class GmlReader implements GioReader {

    public static final String FORMAT_ID = "gml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Graph Modelling Language", ".gml");
    private static final Logger log = LoggerFactory.getLogger(GmlReader.class);
    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseGmlToCjDocument(SingleInputSource inputSource) throws IOException {
        GmlReader gmlReader = new GmlReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document);
        gmlReader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        try (InputStreamReader reader = new InputStreamReader(singleInputSource.inputStream(), StandardCharsets.UTF_8)) {
            GmlReaderHandler handler = new GmlReaderHandler(writer);
            GmlTokenizer tokenizer = new GmlTokenizer(reader, handler);
            tokenizer.parse();
            handler.endDocument();
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }
}
