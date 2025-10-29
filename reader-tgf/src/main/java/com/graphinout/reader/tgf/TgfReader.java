package com.graphinout.reader.tgf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class TgfReader implements GioReader {

    public static final String FORMAT_ID = "tgf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Trivial Graph Format", ".tgf");
    private static final Logger log = LoggerFactory.getLogger(TgfReader.class);
    private static final String DELIMITER_REGEX = "\\s+";
    private static final String SECTION_MARKER = "#";
    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseTgfToCjDocument(SingleInputSource inputSource) throws IOException {
        TgfReader tgfReader = new TgfReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document);
        tgfReader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
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

        if (content.isEmpty()) {
            // Emit empty document (no graphs) for empty TGF content to allow exact empty roundtrip
            writer.document(writer.createDocumentChunk());
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }

    private void ensureNodesExist(final String[] edgeParts, ICjStream writer, final Set<String> nodesCreatedSet) throws IOException {
        for (String nodeId : Arrays.asList(edgeParts[0], edgeParts[1])) {
            if (!nodesCreatedSet.contains(nodeId)) {
                // auto-create nodes if they don't exist
                log.warn("Auto-create node '{}' found in the file for edge: {}", nodeId, Arrays.toString(edgeParts));
                if (errorHandler != null) {
                    errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "No nodes found for edge endpoint: " + nodeId, null));
                }
                ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
                nodeChunk.id(nodeId);
                writer.node(nodeChunk);
                nodesCreatedSet.add(nodeId);
            }
        }
    }

    private void handleWarnings(final boolean edges, final boolean nodes) {
        if (!edges) {
            if (!nodes) {
                if (errorHandler != null) {
                    errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "No nodes found in file", null));
                }
            } else {
                log.warn("No edges found in the file.");
            }
        }
    }

    private void processEdge(final String rawLine, final ICjStream writer, final Set<String> nodesCreatedSet, Consumer<ICjEdgeChunk> edgeConsumer) throws IOException {
        String line = rawLine.trim();
        if (line.isEmpty()) return;
        String[] edgeParts = line.split(DELIMITER_REGEX, 3);
        if (edgeParts.length < 2) return; // invalid edge line
        // ensure nodes exist
        ensureNodesExist(edgeParts, writer, nodesCreatedSet);

        ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
        //source
        edgeChunk.addEndpoint(ep -> ep.node(edgeParts[0]).direction(CjDirection.IN));
        //target
        edgeChunk.addEndpoint(ep -> ep.node(edgeParts[1]).direction(CjDirection.OUT));

        if (edgeParts.length == 3 && !edgeParts[2].isBlank()) {
            edgeChunk.descriptionPlainText(writer.jsonFactory(), edgeParts[2]);
        }
        edgeConsumer.accept(edgeChunk);
    }

    private void processFileContent(final Scanner scanner, final ICjStream writer) throws IOException {
        boolean edges = false;
        boolean nodes = false;


        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        Set<String> nodesCreatedSet = new HashSet<>();

        List<ICjEdgeChunk> edgeBuffer = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue; // skip empty lines
            }
            if (trimmed.equals(SECTION_MARKER)) {
                edges = true;
                continue;
            }
            if (!edges) {
                nodes = true;
                processNode(line, writer, nodesCreatedSet);
            } else {
                processEdge(line, writer, nodesCreatedSet, edgeBuffer::add);
            }
        }

        edgeBuffer.forEach(writer::edge);

        writer.graphEnd();
        writer.documentEnd();

        handleWarnings(edges, nodes);
    }

    private void processNode(final String rawLine, final ICjStream writer, final Set<String> nodesCreatedSet) throws IOException {
        String line = rawLine.trim();
        if (line.isEmpty()) return;
        String[] nodeParts = line.split(DELIMITER_REGEX, 2);
        if (nodeParts.length == 0 || nodeParts[0].isBlank()) return;
        String nodeId = nodeParts[0];
        if (!nodesCreatedSet.contains(nodeId)) {
            nodesCreatedSet.add(nodeId);
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(nodeId);
            if (nodeParts.length == 2 && !nodeParts[1].isBlank()) {
                nodeChunk.descriptionPlainText(writer.jsonFactory(), nodeParts[1]);
            }
            writer.nodeStart(nodeChunk);
            writer.nodeEnd();
        }
    }

}

