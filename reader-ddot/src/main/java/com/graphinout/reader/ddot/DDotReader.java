package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
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
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.value.IntRef;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
 * {@code ddot.it/off} / {@code ddot.it/on} (and their {@code !!off} / {@code !!on} shorthands) disable /
 * re-enable emission of subsequent triples (useful inside templates and code samples). The
 * {@code ddot.it/label} command (shorthand {@code !!label}) — {@code subject ..!!label.. text} — sets the
 * subject node's display label instead of creating an edge (see https://ddot.it/label).
 */
public class DDotReader implements GioReader {

    public static final String FORMAT_ID = "ddot";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "DDot.it Triple Text Format", ".ddot", ".ddot.txt");

    private static final String SEPARATOR_REGEX = "\\s*\\.\\.\\s*";
    private static final String COMMENT_MARKER = "#";
    private static final String SWITCH_OFF = "ddot.it/off";
    private static final String SWITCH_ON = "ddot.it/on";
    /** {@code !!} shorthands for the off/on commands (see https://ddot.it grammar: command = "!!", word). */
    private static final String SWITCH_OFF_SHORT = "!!off";
    private static final String SWITCH_ON_SHORT = "!!on";
    /** Per-link metadata separator (see https://ddot.it): "Meta-data can be appended behind ,,". */
    private static final String META_SEPARATOR = ",,";

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

    /**
     * Get (creating if needed) the buffered node chunk for {@code nodeId}. Nodes are buffered rather than
     * emitted immediately so that node-level facts (label, attributes) parsed on later lines can still be
     * attached. All buffered nodes are emitted (in first-seen order) before the edges.
     */
    private ICjNodeChunkMutable ensureNodeChunk(String nodeId, ICjStream writer,
                                                java.util.Map<String, ICjNodeChunkMutable> nodeBuffer) {
        return nodeBuffer.computeIfAbsent(nodeId, id -> {
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(id);
            return nodeChunk;
        });
    }

    private void processFileContent(Scanner scanner, ICjStream writer) {
        // Document/graph starts are deferred to the end so document-level metadata discovered in the body
        // (ddot.it/this) can be attached to the document chunk before it is emitted.
        ICjDocumentChunkMutable docChunk = writer.createDocumentChunk();
        ICjGraphChunkMutable graphChunk = writer.createGraphChunk();

        java.util.Map<String, ICjNodeChunkMutable> nodeBuffer = new java.util.LinkedHashMap<>();
        List<ICjEdgeChunkMutable> edgeBuffer = new ArrayList<>();
        // per-link metadata, accumulated by identity until all edges are emitted at the end
        Map<ICjEdgeChunkMutable, LinkMeta> edgeMeta = new IdentityHashMap<>();
        boolean enabled = true;
        @Nullable String currentSubject = null;
        // the most recently created link; per-link `,,` metadata (inline or in a following block) attaches here
        @Nullable ICjEdgeChunkMutable currentEdge = null;
        boolean inLinkMeta = false; // inside a multi-line `,,` metadata block bound to currentEdge
        boolean inFreeForm = false; // inside a standalone `,,` block (free-form note; ignored)
        boolean foundAny = false;
        // ddot.it/block: the object value spans the following lines until the next statement
        @Nullable String blockSubject = null;
        @Nullable String blockPredicate = null;
        List<String> blockLines = new ArrayList<>();
        // RDF `prefix` declarations (A ..prefix.. B) collected into the document @context
        LinkedHashMap<String, String> contextMap = new LinkedHashMap<>();

        IntRef lineNumber = intRef(0);
        Locator locator = () -> Location.of(lineNumber.value, 1);

        while (scanner.hasNextLine()) {
            String rawLine = scanner.nextLine();
            lineNumber.value++;
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith(COMMENT_MARKER)) continue;

            if (line.equals(SWITCH_OFF) || line.equals(SWITCH_OFF_SHORT)) {
                enabled = false;
                continue;
            }
            if (line.equals(SWITCH_ON) || line.equals(SWITCH_ON_SHORT)) {
                enabled = true;
                continue;
            }
            if (!enabled) continue;

            // ddot.it/block: gather raw lines as a multi-line object value until the next statement
            // (a line carrying the `..` operator, or a `,,`); see https://ddot.it/block.
            if (blockSubject != null) {
                if (line.contains("..") || line.equals(META_SEPARATOR)) {
                    emitEdge(writer, nodeBuffer, edgeBuffer, blockSubject, blockPredicate, String.join("\n", blockLines));
                    foundAny = true;
                    blockSubject = null;
                    blockPredicate = null;
                    blockLines = new ArrayList<>();
                    currentEdge = null;
                    // fall through: parse this statement line normally
                } else {
                    blockLines.add(line);
                    continue;
                }
            }

            // A lone ",," delimits metadata / free-form blocks (see https://ddot.it).
            if (line.equals(META_SEPARATOR)) {
                if (inLinkMeta) inLinkMeta = false;        // close the link-metadata block
                else if (inFreeForm) inFreeForm = false;   // close the standalone block
                else inFreeForm = true;                    // open a standalone (free-form) block
                continue;
            }
            if (inFreeForm) continue;                      // free-form note content is ignored
            if (inLinkMeta) {                              // a metadata entry for currentEdge
                attachMeta(currentEdge, edgeMeta, line, locator);
                continue;
            }

            // Split off any inline ",," metadata; the part before it is the link itself.
            int comma = line.indexOf(META_SEPARATOR);
            String mainLine = comma >= 0 ? line.substring(0, comma).trim() : line;
            @Nullable String metaPart = comma >= 0 ? line.substring(comma + META_SEPARATOR.length()).trim() : null;

            currentEdge = null; // reset; set again below if this line creates a link

            boolean isContinuation = mainLine.startsWith("..");
            String body = isContinuation ? mainLine.substring(2).trim() : mainLine;

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

            // `ddot.it/this` (shorthand `!!this`) names the current document as subject: its triples are
            // document-level metadata (key -> value), not graph edges. See https://ddot.it/this.
            if (isThisCommand(subject)) {
                docChunk.addProperty(predicate, object);
                foundAny = true;
                continue;
            }

            // Reserved predicates carry node-level facts, not edges. They reconstruct the subject node's
            // existence / display label / attributes; the object is a literal, never a node.
            if (DDotOutput.PRED_NODE.equals(predicate)) {
                ensureNodeChunk(subject, writer, nodeBuffer);
                foundAny = true;
            } else if (isLabelCommand(predicate)) {
                // an inline `,, ..lang.. xx` carries the label's language tag (see https://ddot.it/label)
                ensureNodeChunk(subject, writer, nodeBuffer).addLabel(object, labelLanguage(metaPart));
                foundAny = true;
                if (comma >= 0) continue; // the inline metadata belonged to the label, not an edge
            } else if (predicate.startsWith(DDotOutput.PRED_DATA_PREFIX)) {
                String key = predicate.substring(DDotOutput.PRED_DATA_PREFIX.length());
                ensureNodeChunk(subject, writer, nodeBuffer).addProperty(key, object);
                foundAny = true;
            } else if (DDotOutput.PREFIX_RELATION.equals(predicate)) {
                // `A ..prefix.. B` declares a namespace; collect into the document @context (ids left as-is)
                contextMap.put(subject, object);
                foundAny = true;
            } else if (isTypeRelation(predicate)) {
                // `subject ..has type.. T` (~ rdf:type) sets a node type, not an edge. See https://ddot.it/relations.
                ensureNodeChunk(subject, writer, nodeBuffer).addType(ICjElementType.of(object));
                foundAny = true;
            } else if (isBlockMarker(object)) {
                // ddot.it/block: defer the edge; its object value is gathered from the following lines
                blockSubject = subject;
                blockPredicate = predicate;
                blockLines = new ArrayList<>();
                currentEdge = null;
                foundAny = true;
            } else {
                currentEdge = emitEdge(writer, nodeBuffer, edgeBuffer, subject, predicate, object);
                foundAny = true;
            }

            // Handle the inline / block-opening ",," metadata that followed the link on this line.
            if (comma >= 0) {
                if (metaPart.isEmpty()) {
                    inLinkMeta = true; // trailing ",," opens a multi-line metadata block
                } else {
                    // one or more inline entries separated by further ",,"
                    for (String entry : metaPart.split(java.util.regex.Pattern.quote(META_SEPARATOR))) {
                        if (!entry.trim().isEmpty()) attachMeta(currentEdge, edgeMeta, entry, locator);
                    }
                }
            }
        }

        // a block that runs to end-of-file
        if (blockSubject != null) {
            emitEdge(writer, nodeBuffer, edgeBuffer, blockSubject, blockPredicate, String.join("\n", blockLines));
            foundAny = true;
        }

        if (!foundAny) {
            Nullables.ifConsumerPresentAccept(errorHandler,
                    ContentError.of(ContentError.ErrorLevel.Warn, "Content contains no triples"));
        }
        // Now emit, in CJ stream order: document (with any ddot.it/this metadata + @context), graph, nodes, edges.
        if (!contextMap.isEmpty()) docChunk.context(contextMap);
        writer.documentStart(docChunk);
        writer.graphStart(graphChunk);
        nodeBuffer.values().forEach(writer::node);
        // attach accumulated per-link metadata, namespaced under "ddot-it:props" / "ddot-it:text"
        edgeBuffer.forEach(edge -> writeMeta(edge, edgeMeta.get(edge), writer.jsonFactory()));
        edgeBuffer.forEach(writer::edge);

        writer.graphEnd();
        writer.documentEnd();
    }

    /** Accumulator for one link's metadata: structured props (key -&gt; value) and free-text notes. */
    private static final class LinkMeta {
        final LinkedHashMap<String, String> props = new LinkedHashMap<>();
        final List<String> texts = new ArrayList<>();

        boolean isEmpty() {
            return props.isEmpty() && texts.isEmpty();
        }
    }

    /**
     * Route one per-link metadata entry into {@code edge}'s accumulator. A structured entry
     * ({@code ..key.. value}, the leading {@code ..} optional) becomes a {@code key -> value} property; an
     * unstructured entry (free text with no {@code ..} separator, e.g. {@code ,, a random note}) becomes a note.
     */
    private void attachMeta(@Nullable ICjEdgeChunkMutable edge, Map<ICjEdgeChunkMutable, LinkMeta> edgeMeta,
                            String entry, Locator locator) {
        String t = entry.trim();
        if (t.isEmpty()) return;
        if (edge == null) {
            sendIssue(ContentError.ErrorLevel.Warn, "Link metadata without a preceding link; skipping: " + entry, locator);
            return;
        }
        LinkMeta meta = edgeMeta.computeIfAbsent(edge, e -> new LinkMeta());
        String body = t.startsWith("..") ? t.substring(2).trim() : t;
        String[] p = body.split(SEPARATOR_REGEX, -1);
        if (p.length == 2 && !p[0].trim().isEmpty() && !p[1].trim().isEmpty()) {
            meta.props.put(p[0].trim(), p[1].trim());   // structured "key .. value"
        } else {
            meta.texts.add(t);                          // free-form note
        }
    }

    /** Write a link's accumulated metadata onto the edge as {@code {"ddot-it:props": {...}, "ddot-it:text": ...}}. */
    private void writeMeta(ICjEdgeChunkMutable edge, @Nullable LinkMeta meta, IJsonFactory jf) {
        if (meta == null || meta.isEmpty()) return;
        IJsonObjectMutable data = jf.createObjectMutable();
        if (!meta.props.isEmpty()) {
            IJsonObjectMutable props = jf.createObjectMutable();
            meta.props.forEach(props::add);
            data.addProperty(DDotOutput.LINK_PROPS_KEY, props);
        }
        if (!meta.texts.isEmpty()) {
            if (meta.texts.size() == 1) {
                data.add(DDotOutput.LINK_TEXT_KEY, meta.texts.get(0));
            } else {
                IJsonArrayMutable arr = jf.createArrayMutable();
                meta.texts.forEach(arr::add);
                data.addProperty(DDotOutput.LINK_TEXT_KEY, arr);
            }
        }
        edge.dataMutable(d -> d.setJsonValue(data));
    }

    /**
     * Is this predicate the "set node label" command? Accepts graphinout's own round-trip form
     * ({@code ddot:label}), the ddot.it-native command ({@code ddot.it/label}) and its shorthand
     * ({@code !!label}). See https://ddot.it/label.
     */
    private static boolean isLabelCommand(String predicate) {
        return DDotOutput.PRED_LABEL.equals(predicate)
                || "ddot.it/label".equals(predicate)
                || "!!label".equals(predicate);
    }

    /** Is this subject the "current document" command {@code ddot.it/this} (shorthand {@code !!this})? */
    private static boolean isThisCommand(String subject) {
        return DDotOutput.SUBJECT_THIS.equals(subject) || "!!this".equals(subject);
    }

    /** Is this object the multi-line block-literal marker {@code ddot.it/block} (shorthand {@code !!block})? */
    private static boolean isBlockMarker(String object) {
        return DDotOutput.OBJECT_BLOCK.equals(object) || "!!block".equals(object);
    }

    /** ddot.it's suggested standard relation aliases → canonical names (see https://ddot.it/relations). */
    private static final Map<String, String> RELATION_ALIASES = Map.ofEntries(
            Map.entry("rel", "related"), Map.entry("is related", "related"),
            Map.entry("is same as", "same as"),
            Map.entry("link", "links to"), Map.entry("see also", "links to"),
            Map.entry("tag", "has tag"),
            Map.entry("type", "has type"), Map.entry("is a", "has type"),
            Map.entry("subtype", "has subtype"),
            Map.entry("content", "has content"));

    /** Map a relation alias to its canonical name; non-aliases (incl. the empty untyped link) pass through. */
    private static String canonicalRelation(String predicate) {
        return RELATION_ALIASES.getOrDefault(predicate, predicate);
    }

    /** Is this predicate the rdf:type-like relation ({@code has type}, or aliases {@code is a}/{@code type})? */
    private static boolean isTypeRelation(String predicate) {
        return DDotOutput.TYPE_RELATION.equals(canonicalRelation(predicate));
    }

    /** Extract the label language from an inline {@code ..lang.. xx} metadata entry, or {@code null}. */
    private static @Nullable String labelLanguage(@Nullable String metaPart) {
        if (metaPart == null) return null;
        String body = metaPart.startsWith("..") ? metaPart.substring(2).trim() : metaPart;
        String[] p = body.split(SEPARATOR_REGEX, -1);
        return (p.length == 2 && "lang".equals(p[0].trim()) && !p[1].trim().isEmpty()) ? p[1].trim() : null;
    }

    /** Create and buffer a directed edge {@code subject --predicate--> object} (empty predicate = untyped). */
    private ICjEdgeChunkMutable emitEdge(ICjStream writer, java.util.Map<String, ICjNodeChunkMutable> nodeBuffer,
                                         List<ICjEdgeChunkMutable> edgeBuffer, String subject, String predicate, String object) {
        ensureNodeChunk(subject, writer, nodeBuffer);
        ensureNodeChunk(object, writer, nodeBuffer);
        ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
        edgeChunk.addEndpoint(ep -> ep.node(subject).direction(CjDirection.IN));
        edgeChunk.addEndpoint(ep -> ep.node(object).direction(CjDirection.OUT));
        // `....` stays untyped (empty predicate); a named relation is canonicalised from its aliases
        String relation = canonicalRelation(predicate);
        if (!relation.isEmpty()) {
            edgeChunk.edgeType(relation);
            edgeChunk.addLabelWithoutLanguage(relation);
        }
        edgeBuffer.add(edgeChunk);
        return edgeChunk;
    }

    private void sendIssue(ContentError.ErrorLevel level, String msg, Locator locator) {
        ContentError err = ContentError.of(level, msg, locator.location());
        Nullables.ifConsumerPresentAccept(errorHandler, err);
    }
}
