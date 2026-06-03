package com.graphinout.reader.d2;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class D2Reader implements GioReader {

    public static final String FORMAT_ID = "d2";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "D2 (Terrastruct)", ".d2");

    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseD2ToCjDocument(SingleInputSource inputSource) throws IOException {
        D2Reader reader = new D2Reader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document, true);
        reader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) throw new IllegalArgumentException("Cannot handle multi-sources");
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);

        if (content.isBlank()) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content is empty"));
            writer.document(writer.createDocumentChunk());
            return;
        }

        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        D2Lines2CjDocument parser = new D2Lines2CjDocument(writer, errorHandler);
        parser.parse(content);

        if (!parser.hasContent()) {
            Nullables.ifConsumerPresentAccept(errorHandler,
                    ContentError.of(ContentError.ErrorLevel.Warn, "Diagram has no nodes or edges"));
        }
        parser.flush();

        writer.graphEnd();
        writer.documentEnd();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }
}
