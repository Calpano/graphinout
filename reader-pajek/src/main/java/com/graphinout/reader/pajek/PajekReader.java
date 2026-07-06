package com.graphinout.reader.pajek;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.value.IntRef;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.value.IntRef.intRef;

public class PajekReader implements GioReader {

    public static final String FORMAT_ID = "pajek";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Pajek Network Format", ".net");

    private enum Section { NONE, VERTICES, ARCS, ARCSLIST, EDGES, EDGESLIST }

    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parsePajekToCjDocument(SingleInputSource inputSource) throws IOException {
        PajekReader reader = new PajekReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(inputSource, new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
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
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isBlank()) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content is empty"));
            writer.document(writer.createDocumentChunk());
            return;
        }
        parseContent(content, writer);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void parseContent(String content, ICjStream writer) throws IOException {
        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        String[] lines = content.split("\\r?\\n");
        IntRef lineNumber = intRef(0);
        Locator locator = () -> Location.of(lineNumber.value, 1);

        Section currentSection = Section.NONE;

        for (String line : lines) {
            lineNumber.value++;
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("%")) continue;

            String upper = trimmed.toUpperCase();
            if (upper.startsWith("*VERTICES")) {
                currentSection = Section.VERTICES;
                continue;
            }
            if (upper.startsWith("*ARCSLIST")) {
                currentSection = Section.ARCSLIST;
                continue;
            }
            if (upper.startsWith("*ARCS")) {
                // handles "*Arcs", "*Arcs :1 \"label\"" (multi-relational)
                currentSection = Section.ARCS;
                continue;
            }
            if (upper.startsWith("*EDGESLIST")) {
                currentSection = Section.EDGESLIST;
                continue;
            }
            if (upper.startsWith("*EDGES")) {
                // handles "*Edges", "*Edges :1 \"label\"" (multi-relational)
                currentSection = Section.EDGES;
                continue;
            }
            if (trimmed.startsWith("*")) {
                // Unknown section — skip its content
                currentSection = Section.NONE;
                continue;
            }

            switch (currentSection) {
                case VERTICES -> parseVertex(trimmed, writer, locator);
                case ARCS -> parseEdge(trimmed, writer, true, locator);
                case ARCSLIST -> parseEdgeList(trimmed, writer, true, locator);
                case EDGES -> parseEdge(trimmed, writer, false, locator);
                case EDGESLIST -> parseEdgeList(trimmed, writer, false, locator);
                default -> {} // data before any section header — ignore
            }
        }

        writer.graphEnd();
        writer.documentEnd();
    }

    private void parseVertex(String line, ICjStream writer, Locator locator) {
        // Format: id ["label"] [x y [z]]
        String[] parts = line.split("\\s+", 2);
        if (parts.length == 0 || parts[0].isBlank()) {
            sendIssue(ContentError.ErrorLevel.Warn, "Invalid vertex line: " + line, locator);
            return;
        }
        ICjNodeChunkMutable node = writer.createNodeChunk();
        node.id(parts[0]);

        if (parts.length > 1) {
            String rest = parts[1].trim();
            if (rest.startsWith("\"")) {
                int endQuote = rest.indexOf('"', 1);
                String label = endQuote > 0 ? rest.substring(1, endQuote) : rest.substring(1);
                node.addLabelWithoutLanguage(label);
            } else {
                // Unquoted: first token is the label, the rest are coordinates
                String firstToken = rest.split("\\s+", 2)[0];
                if (!firstToken.isBlank()) {
                    node.addLabelWithoutLanguage(firstToken);
                }
            }
        }

        writer.nodeStart(node);
        writer.nodeEnd();
    }

    private void parseEdge(String line, ICjStream writer, boolean directed, Locator locator) {
        // Format: from to [weight]
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            sendIssue(ContentError.ErrorLevel.Warn, "Invalid edge line: " + line, locator);
            return;
        }
        String fromId = parts[0];
        String toId = parts[1];

        ICjEdgeChunkMutable edge = writer.createEdgeChunk();
        if (directed) {
            edge.addEndpoint(ep -> ep.node(fromId).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(toId).direction(CjDirection.OUT));
        } else {
            edge.addEndpoint(ep -> ep.node(fromId).direction(CjDirection.UNDIR));
            edge.addEndpoint(ep -> ep.node(toId).direction(CjDirection.UNDIR));
        }
        writer.edge(edge);
    }

    private void parseEdgeList(String line, ICjStream writer, boolean directed, Locator locator) {
        // Format: from to1 to2 to3 ...  (source connects to every listed target)
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            sendIssue(ContentError.ErrorLevel.Warn, "Invalid edge-list line: " + line, locator);
            return;
        }
        String fromId = parts[0];
        for (int i = 1; i < parts.length; i++) {
            String toId = parts[i];
            ICjEdgeChunkMutable edge = writer.createEdgeChunk();
            if (directed) {
                edge.addEndpoint(ep -> ep.node(fromId).direction(CjDirection.IN));
                edge.addEndpoint(ep -> ep.node(toId).direction(CjDirection.OUT));
            } else {
                edge.addEndpoint(ep -> ep.node(fromId).direction(CjDirection.UNDIR));
                edge.addEndpoint(ep -> ep.node(toId).direction(CjDirection.UNDIR));
            }
            writer.edge(edge);
        }
    }

    private void sendIssue(ContentError.ErrorLevel level, String msg, Locator locator) {
        Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(level, msg, locator.location()));
    }
}
