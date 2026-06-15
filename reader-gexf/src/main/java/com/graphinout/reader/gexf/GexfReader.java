package com.graphinout.reader.gexf;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.xml.sax.Sax2XmlWriter;
import com.graphinout.base.xml.util.XmlTool;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class GexfReader implements GioReader {

    public static final String FORMAT_ID = "gexf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GEXF Format", ".gexf", ".gexf.xml");
    private static final Logger log = LoggerFactory.getLogger(GexfReader.class);
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        // Reuse the GraphML XML architecture: SAX -> Sax2XmlWriter -> Xml2GexfCjWriter -> ICjStream
        Xml2GexfCjWriter xml2Gexf = new Xml2GexfCjWriter(cjStream);
        xml2Gexf.setContentErrorHandler(errorHandler);
        Sax2XmlWriter saxHandler = new Sax2XmlWriter(xml2Gexf);
        try {
            XMLReader reader = XmlTool.createXmlReaderOn(saxHandler);
            try (InputStream in = singleInputSource.inputStream()) {
                reader.parse(new org.xml.sax.InputSource(in));
            }
        } catch (SAXException | ParserConfigurationException e) {
            Nullables.ifConsumerPresentAccept(errorHandler, //
                    ContentError.of(ContentError.ErrorLevel.Error, "Failed to parse GEXF '" + inputSource.name() + "': " + e.getMessage()));
        } catch (ContentErrorException e) {
            // already reported via the content error handler
            log.warn("ContentError while reading GEXF", e);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
