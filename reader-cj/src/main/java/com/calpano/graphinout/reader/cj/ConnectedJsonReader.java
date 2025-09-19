package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.reader.graphml.GraphmlReader;
import com.calpano.graphinout.reader.graphml.cj.Cj2GraphmlWriter;

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

        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();

        // we won't build a CJ to GIO Reader
        // so we reuse the path CJ -> GraphML -> GIO, until Graphinout replaced GIO by CJ
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GraphmlWriter graphml2gioWriter = new Graphml2XmlWriter(xmlWriter);
        Cj2GraphmlWriter cj2Graphml = new Cj2GraphmlWriter(graphml2gioWriter);
        Json2CjWriter json2Cj = new Json2CjWriter(cj2Graphml);
        try {
            jsonReader.read(inputSource, json2Cj);
            String xml = xmlWriter.string();
            GraphmlReader graphmlReader = new GraphmlReader();
            graphmlReader.read(SingleInputSourceOfString.of("graphml", xml), writer);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON content", e);
        }
    }

}
