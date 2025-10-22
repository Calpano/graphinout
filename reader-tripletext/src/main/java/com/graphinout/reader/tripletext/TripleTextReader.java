package com.graphinout.reader.example;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDataType;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioEndpoint;
import com.graphinout.base.gio.GioEndpointDirection;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioKeyForType;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.XmlFragmentString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class TripleTextReader implements GioReader {

    public static final String FORMAT_ID = "tripletext";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "TripleText Format", ".tt", "triple.txt", ".tripletext");
    private Consumer<ContentError> errorHandler;

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
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        TripleTextModel tripleTextModel = new TripleTextModel();
        try (InputStreamReader isr = new InputStreamReader(sis.inputStream(), StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                TripleText.parseLine(line, (s, p, o, m) -> {
                    tripleTextModel.indexTriple(s, p, o);
                    tripleTextModel.indexNode(o);
                });
            }
        }

        // and write the graph to our GioWriter
        writer.startDocument(GioDocument.builder().build());

        // declare keys
        writer.key(GioKey.builder().id("label").forType(GioKeyForType.Node).attributeName("label").attributeType(GioDataType.typeString).build());

        writer.startGraph(GioGraph.builder().build());

        // write nodes
        for (TripleTextModel.Node node : tripleTextModel.nodes()) {
            writer.startNode(GioNode.builder().id(node.id).build());
            if (node.label != null)
                writer.data(GioData.builder().id("label").xmlValue(XmlFragmentString.ofPlainText(node.label)).build());
            writer.endNode(null);
        }

        // write edges
        tripleTextModel.forEachTriple((sNode, p, o, meta) -> {
            assert sNode != null;
            assert sNode.id != null;
            assert p != null;
            assert !p.isBlank();
            assert o != null;
            try {
                GioEndpoint sEndpoint = GioEndpoint.builder().node(sNode.id).type(GioEndpointDirection.Out).build();
                GioEndpoint oEndpoint = GioEndpoint.builder().node(o).type(GioEndpointDirection.In).build();
                writer.startEdge(GioEdge.builder().endpoints(Arrays.asList(sEndpoint, oEndpoint)).build());
                GioData.GioDataBuilder gioDataBuilder = GioData.builder().key("label");
                writer.data(gioDataBuilder.xmlValue(XmlFragmentString.ofPlainText(p)).build());
                // TODO meta
                writer.endEdge();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.endGraph(null);
        writer.endDocument();
    }

}
