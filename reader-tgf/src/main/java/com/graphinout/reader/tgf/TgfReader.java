package com.graphinout.reader.tgf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.base.input.InputSource;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.value.IntRef;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.value.IntRef.intRef;

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
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content is empty"));
            writer.document(writer.createDocumentChunk());
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /** auto-create nodes if they don't exist */
    private void ensureNodesExist(final String[] edgeParts, ICjStream writer, final Set<String> nodesCreatedSet, Locator locator) {
        for (String nodeId : List.of(edgeParts[0], edgeParts[1])) {
            if (!nodesCreatedSet.contains(nodeId)) {
                String msg = String.format("Auto-create node '%s' found in the file for edge: %s", nodeId, Arrays.toString(edgeParts));
                sendIssue(ContentError.ErrorLevel.Warn, msg, locator);
                ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
                nodeChunk.id(nodeId);
                writer.node(nodeChunk);
                nodesCreatedSet.add(nodeId);
            }
        }
    }

    private void processEdge(final String rawLine, final ICjStream writer, final Set<String> nodesCreatedSet, Locator locator, Consumer<ICjEdgeChunk> edgeConsumer) throws IOException {
        String line = rawLine.trim();
        if (line.isEmpty()) {
            sendIssue(ContentError.ErrorLevel.Info, "Skipping empty edge", locator);
            return;
        }
        String[] edgeParts = line.split(DELIMITER_REGEX, 3);
        if (edgeParts.length < 2) {
            sendIssue(ContentError.ErrorLevel.Info, "Skipping invalid edge", locator);
            return; // invalid edge line
        }
        ensureNodesExist(edgeParts, writer, nodesCreatedSet, locator);

        ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
        //source
        edgeChunk.addEndpoint(ep -> ep.node(edgeParts[0]).direction(CjDirection.IN));
        //target
        edgeChunk.addEndpoint(ep -> ep.node(edgeParts[1]).direction(CjDirection.OUT));

        if (edgeParts.length == 3 && !edgeParts[2].isBlank()) {
            edgeChunk.addLabelWithoutLanguage(edgeParts[2]);
        }
        edgeConsumer.accept(edgeChunk);
    }

    private void processFileContent(final Scanner scanner, final ICjStream writer) throws IOException {
        boolean foundEdges = false;
        boolean foundNodes = false;

        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        Set<String> nodesCreatedSet = new HashSet<>();

        List<ICjEdgeChunk> edgeBuffer = new ArrayList<>();
        IntRef lineNumber = intRef(0);
        Locator locator = () -> Location.of(lineNumber.value, 1);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineNumber.value++;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue; // skip empty lines
            }
            if (trimmed.equals(SECTION_MARKER)) {
                foundEdges = true;
                continue;
            }
            if (!foundEdges) {
                foundNodes = true;
                processNode(line, writer, nodesCreatedSet, locator);
            } else {
                processEdge(line, writer, nodesCreatedSet, locator, edgeBuffer::add);
            }
        }

        if (!foundNodes) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content contains no nodes"));
        }
        if (!foundEdges) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content contains no edges"));
        }
        edgeBuffer.forEach(writer::edge);

        writer.graphEnd();
        writer.documentEnd();
    }

    private void processNode(final String rawLine, final ICjStream writer, final Set<String> nodesCreatedSet, Locator locator) {
        String line = rawLine.trim();
        if (line.isEmpty()) return;
        String[] nodeParts = line.split(DELIMITER_REGEX, 2);
        if (nodeParts.length == 0 || nodeParts[0].isBlank()) {
            sendIssue(ContentError.ErrorLevel.Info, "Skipping empty node", locator);
            return;
        }
        String nodeId = nodeParts[0];
        boolean isNew = nodesCreatedSet.add(nodeId);
        if (!isNew) {
            sendIssue(ContentError.ErrorLevel.Warn, "Skipping duplicate node ID: " + nodeId, locator);
        } else {
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(nodeId);
            // add optional label
            if (nodeParts.length == 2 && !nodeParts[1].isBlank()) {
                nodeChunk.addLabelWithoutLanguage(nodeParts[1]);
            }
            writer.nodeStart(nodeChunk);
            writer.nodeEnd();
        }
    }

    private void sendIssue(ContentError.ErrorLevel errorLevel, String msg, Locator locator) {
        ContentError value = ContentError.of(errorLevel, msg, locator.location());
        Nullables.ifConsumerPresentAccept(errorHandler, value);
    }

}

