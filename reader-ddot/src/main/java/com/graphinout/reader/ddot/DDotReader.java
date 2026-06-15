package com.graphinout.reader.ddot;

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
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.foundation.pure.value.IntRef;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.value.IntRef.intRef;

/**
 * Reads the DDot.it simple triple text format.
 * <p>
 * Each non-blank, non-comment line is either a full triple
 * <pre>subject .. predicate .. object</pre>
 * or a continuation line that inherits the subject from the preceding triple
 * <pre>.. predicate .. object</pre>
 * Lines starting with {@code #} are comments. The switches
 * {@code ddot.it/off} and {@code ddot.it/on} disable / re-enable emission of
 * subsequent triples (useful inside templates and code samples).
 */
public class DDotReader implements GioReader {

    public static final String FORMAT_ID = "ddot";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "DDot.it Triple Text Format", ".ddot", ".ddot.txt");

    private static final String SEPARATOR_REGEX = "\\s*\\.\\.\\s*";
    private static final String COMMENT_MARKER = "#";
    private static final String SWITCH_OFF = "ddot.it/off";
    private static final String SWITCH_ON = "ddot.it/on";

    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseDDotToCjDocument(SingleInputSource inputSource) throws IOException {
        DDotReader reader = new DDotReader();
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
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
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

    private void ensureNode(String nodeId, ICjStream writer, Set<String> nodesCreatedSet) {
        if (nodesCreatedSet.add(nodeId)) {
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(nodeId);
            writer.node(nodeChunk);
        }
    }

    private void processFileContent(Scanner scanner, ICjStream writer) {
        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        Set<String> nodesCreatedSet = new HashSet<>();
        List<ICjEdgeChunk> edgeBuffer = new ArrayList<>();
        boolean enabled = true;
        @Nullable String currentSubject = null;
        boolean foundAny = false;

        IntRef lineNumber = intRef(0);
        Locator locator = () -> Location.of(lineNumber.value, 1);

        while (scanner.hasNextLine()) {
            String rawLine = scanner.nextLine();
            lineNumber.value++;
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith(COMMENT_MARKER)) continue;

            if (line.equals(SWITCH_OFF)) {
                enabled = false;
                continue;
            }
            if (line.equals(SWITCH_ON)) {
                enabled = true;
                continue;
            }
            if (!enabled) continue;

            boolean isContinuation = line.startsWith("..");
            String body = isContinuation ? line.substring(2).trim() : line;

            String[] parts = body.split(SEPARATOR_REGEX, -1);

            String subject;
            String predicate;
            String object;
            if (isContinuation) {
                if (currentSubject == null) {
                    sendIssue(ContentError.ErrorLevel.Warn,
                            "Continuation line without prior subject; skipping: " + rawLine, locator);
                    continue;
                }
                if (parts.length != 2) {
                    sendIssue(ContentError.ErrorLevel.Warn,
                            "Invalid continuation (expected 'predicate .. object'): " + rawLine, locator);
                    continue;
                }
                subject = currentSubject;
                predicate = parts[0].trim();
                object = parts[1].trim();
            } else {
                if (parts.length != 3) {
                    sendIssue(ContentError.ErrorLevel.Warn,
                            "Invalid triple (expected 'subject .. predicate .. object'): " + rawLine, locator);
                    continue;
                }
                subject = parts[0].trim();
                predicate = parts[1].trim();
                object = parts[2].trim();
                currentSubject = subject;
            }
            // subject and object are mandatory; an empty predicate is a valid "untyped link" (e.g. "a .. .. b")
            if (subject.isEmpty() || object.isEmpty()) {
                sendIssue(ContentError.ErrorLevel.Warn,
                        "Triple has empty subject/object; skipping: " + rawLine, locator);
                continue;
            }

            ensureNode(subject, writer, nodesCreatedSet);
            ensureNode(object, writer, nodesCreatedSet);

            ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
            final String subjectFinal = subject;
            final String objectFinal = object;
            edgeChunk.addEndpoint(ep -> ep.node(subjectFinal).direction(CjDirection.IN));
            edgeChunk.addEndpoint(ep -> ep.node(objectFinal).direction(CjDirection.OUT));
            // an untyped link carries no predicate, hence no edge label
            if (!predicate.isEmpty()) {
                edgeChunk.addLabelWithoutLanguage(predicate);
            }
            edgeBuffer.add(edgeChunk);

            foundAny = true;
        }

        if (!foundAny) {
            Nullables.ifConsumerPresentAccept(errorHandler,
                    ContentError.of(ContentError.ErrorLevel.Warn, "Content contains no triples"));
        }
        edgeBuffer.forEach(writer::edge);

        writer.graphEnd();
        writer.documentEnd();
    }

    private void sendIssue(ContentError.ErrorLevel level, String msg, Locator locator) {
        ContentError err = ContentError.of(level, msg, locator.location());
        Nullables.ifConsumerPresentAccept(errorHandler, err);
    }
}
