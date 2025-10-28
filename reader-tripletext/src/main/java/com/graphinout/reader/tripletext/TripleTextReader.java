package com.graphinout.reader.tripletext;

import com.graphinout.base.GioReader;
import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjEdgeTypeSource;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class TripleTextReader implements GioReader {

    public static final String FORMAT_ID = "tripletext";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "TripleText Format", ".tt.txt", ".tt", "triple.txt", ".tripletext");
    private Consumer<ContentError> errorHandler;

    public static ICjDocument parseToCjDocument(SingleInputSource input) throws IOException {
        TripleTextReader reader = new TripleTextReader();
        CjWriter2CjDocumentWriter elementsWriter = new CjWriter2CjDocumentWriter();
        CjStream2CjWriter streamToWriter = new CjStream2CjWriter(elementsWriter);
        reader.read(input, streamToWriter);
        return elementsWriter.resultDoc();
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
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        TripleTextModel tripleTextModel = new TripleTextModel();
        try (InputStreamReader isr = new InputStreamReader(sis.inputStream(), StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                TripleText.parseLine(line, (s, p, o, m) -> {
                    tripleTextModel.indexTriple(s, p, o);
                });
            }
        }

        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        // write nodes
        for (TripleTextModel.Node node : tripleTextModel.nodes()) {
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(node.id);

            // TODO make simpkler to do this
            ifPresentAccept(node.label, label -> {
                nodeChunk.setLabel(l -> l.addEntry(entry -> {
                    entry.value(label);
                    // we dont know the language
                }));
            });

            writer.node(nodeChunk);
        }

        // write edges
        tripleTextModel.forEachTriple((sNode, p, o, meta) -> {
            assert sNode != null;
            assert sNode.id != null;
            assert p != null;
            assert !p.isBlank();
            assert o != null;

            ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();

            edgeChunk.addEndpoint(ep -> {
                ep.node(sNode.id);
                ep.direction(CjDirection.IN);
            });
            edgeChunk.addEndpoint(ep -> {
                ep.node(o);
                ep.direction(CjDirection.OUT);
            });
            edgeChunk.edgeType(ICjEdgeType.of(CjEdgeTypeSource.String, p));

            // TripleText edge meta
            if (meta != null && !meta.isBlank()) {
                edgeChunk.dataMutable(data -> {
                    data.addProperty("tt:meta", writer.jsonFactory().createString(meta));
                });
            }
            writer.edge(edgeChunk);
        });

        writer.graphEnd();
        writer.documentEnd();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
