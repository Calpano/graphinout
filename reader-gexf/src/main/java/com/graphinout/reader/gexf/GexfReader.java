package com.graphinout.reader.gexf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.function.Consumer;

public class GexfReader implements GioReader {

    public static final String FORMAT_ID = "gexf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GEXF Text Format", ".gexf", ".gexf.txt");
    private Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Gexf2CjWriter handler = new Gexf2CjWriter(cjStream);
            saxParser.parse(inputSource.asSingle().inputStream(), handler);
        } catch (Exception e) {
            throw new IOException("Error parsing GEXF file", e);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }
}
