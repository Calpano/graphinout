package com.graphinout.reader.ddot;

import com.calpano.ddot.it.event.DdotEvent;
import com.calpano.ddot.it.event.DdotEventExporter;
import com.calpano.ddot.it.refactor.DdotLineSerializer;
import com.calpano.ddot.it.source.DdotCommands;
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
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reads the ddot.it triple text format into Connected JSON.
 * <p>
 * <strong>This class does not implement the ddot.it grammar.</strong> The grammar lives in
 * {@code com.calpano.ddot.it:ddot-core}, whose {@link DdotEventExporter} is a fold over the canonical
 * {@code DdotTokenizer} and is pinned to the cross-implementation golden corpus (35 cases, see
 * {@link DDotCorpusConformanceTest}). This reader consumes the resulting
 * {@link DdotEvent} stream — {@code from} / {@code type} / {@code to} / {@code meta} / {@code location} —
 * and folds it into the CJ node/edge/document stream. Everything the format itself defines (the exact
 * two-dot runs, the {@code .... } untyped form, the line gate that keeps prose out, {@code !!off}/{@code !!on}
 * regions incl. the host-comment spelling, subject inheritance on continuation lines, {@code !!block}
 * bodies, inline and multi-line {@code ,,} metadata and its {@code ;;} separator) is handled there, once,
 * for every ddot.it implementation.
 * <p>
 * What remains here is graphinout's <em>vocabulary</em> layer, which is not part of the ddot.it grammar:
 * <ul>
 *   <li>{@code ddot.it/this} (any spelling) as the subject ⇒ document-level metadata (https://ddot.it/this)</li>
 *   <li>{@code label} ⇒ node display label; {@code prefix} ⇒ document {@code @context} (https://ddot.it/rdf)</li>
 *   <li>{@code has type} and its aliases ⇒ a node type; the other {@link #RELATION_ALIASES} ⇒ canonical names
 *       (https://ddot.it/relations)</li>
 *   <li>RDF literal markers in the {@code ,,} metadata ⇒ a node {@code rdf:data} literal (doc/spec-ddot-rdf.adoc E1)</li>
 *   <li>graphinout's own round-trip predicates {@code ddot:node} / {@code ddot:label} / {@code ddot:data:*}</li>
 * </ul>
 */
public class DDotReader implements GioReader {

    public static final String FORMAT_ID = "ddot";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "DDot.it Triple Text Format", ".ddot", ".ddot.txt");

    /** The {@code kind} context field of the emitted ddot.it events (see https://ddot.it/developer-guide.html#events). */
    private static final String EVENT_KIND = "ddot";

    /**
     * The built-in relation ddot.it gives to free metadata text ({@code ,, a note}), as opposed to a
     * structured pair ({@code ,, ..key.. value}). See the Parse Spec, Information Model.
     */
    private static final String META_TEXT_RELATION = "text";

    /** ddot.it's suggested standard relation aliases → canonical names (see https://ddot.it/relations). */
    private static final Map<String, String> RELATION_ALIASES = Map.ofEntries(
            Map.entry("rel", "related"), Map.entry("is related", "related"),
            Map.entry("is same as", "same as"),
            Map.entry("link", "links to"), Map.entry("see also", "links to"),
            Map.entry("tag", "has tag"),
            Map.entry("type", "has type"), Map.entry("is a", "has type"),
            Map.entry("subtype", "has subtype"),
            Map.entry("content", "has content"),
            // UML relationship aliases: the simple visual tokens map to the standard UML name (see PlantUmlReader).
            Map.entry("solid-to", "association"), Map.entry("solid", "association"),
            Map.entry("solid-both", "association"), Map.entry("directed", "association"),
            Map.entry("dashed-to", "dependency"), Map.entry("dashed", "dependency"),
            Map.entry("dashed-both", "dependency"),
            Map.entry("extends", "generalization"), Map.entry("extension", "generalization"),
            Map.entry("inheritance", "generalization"), Map.entry("generalizes", "generalization"),
            Map.entry("realizes", "realization"), Map.entry("implements", "realization"),
            Map.entry("composed of", "composition"), Map.entry("composes", "composition"),
            Map.entry("aggregates", "aggregation"), Map.entry("has a", "aggregation"));

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
        // The one and only parse: ddot-core owns the grammar; this class only interprets the events.
        collect(DdotEventExporter.parse(content, EVENT_KIND, singleInputSource.name()), writer);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Fold the ddot.it event stream into the CJ stream.
     * <p>
     * Document/graph starts are deferred to the end so that facts discovered in the body — {@code
     * ddot.it/this} document metadata and {@code prefix} declarations — can still be attached to the
     * document chunk before it is emitted. Nodes are likewise buffered so a label or attribute parsed on a
     * later line still lands on the node it belongs to.
     */
    private void collect(List<DdotEvent> events, ICjStream writer) {
        ICjDocumentChunkMutable docChunk = writer.createDocumentChunk();
        ICjGraphChunkMutable graphChunk = writer.createGraphChunk();

        Map<String, ICjNodeChunkMutable> nodeBuffer = new LinkedHashMap<>();
        List<ICjEdgeChunkMutable> edgeBuffer = new ArrayList<>();
        // per-link metadata, accumulated by identity until all edges are emitted at the end
        Map<ICjEdgeChunkMutable, LinkMeta> edgeMeta = new IdentityHashMap<>();
        // RDF `prefix` declarations (A ..prefix.. B) collected into the document @context
        LinkedHashMap<String, String> contextMap = new LinkedHashMap<>();

        for (DdotEvent event : events) {
            Locator locator = () -> Location.of(event.location, 1);
            String subject = event.from;
            String object = event.to;
            // An event with no `type` is ddot.it's untyped link (`a .... b`); CJ models it as an edge
            // without a type, so the empty string is the natural in-reader spelling.
            String predicate = event.type == null ? "" : event.type;

            // A `!!block` that never received a body yields an empty value; it cannot name a node.
            if (subject.isBlank() || object.isBlank()) {
                sendIssue(ContentError.ErrorLevel.Warn,
                        "Triple has empty subject/object; skipping: " + subject + " .." + predicate + ".. " + object,
                        locator);
                continue;
            }

            // `ddot.it/this` (shorthand `!!this`) names the current document as subject: its triples are
            // document-level metadata (key -> value), not graph edges. See https://ddot.it/this.
            if (isThisCommand(subject)) {
                docChunk.addProperty(predicate, object);
                continue;
            }

            // An object flagged as an RDF literal (`,, ..rdf:datatype/language/literal.. …`) is a node data
            // property under `rdf:data`, NOT an edge to a resource. See doc/spec-ddot-rdf.adoc E1.
            IJsonValue literal = literalValue(object, event.meta, writer.jsonFactory());
            if (literal != null) {
                ensureNodeChunk(subject, writer, nodeBuffer).dataMutable(d ->
                        d.add(IJsonContainerNavigationStep.pathOf(DDotOutput.RDF_DATA_KEY, predicate), literal));
                continue;
            }

            // Reserved predicates carry node-level facts, not edges. They reconstruct the subject node's
            // existence / display label / attributes; the object is a literal, never a node.
            if (DDotOutput.PRED_NODE.equals(predicate)) {
                ensureNodeChunk(subject, writer, nodeBuffer);
            } else if (isLabelCommand(predicate)) {
                // a `,, ..lang.. xx` entry carries the label's language tag (see https://ddot.it/label)
                ensureNodeChunk(subject, writer, nodeBuffer).addLabel(object, labelLanguage(event.meta));
            } else if (predicate.startsWith(DDotOutput.PRED_DATA_PREFIX)) {
                String key = predicate.substring(DDotOutput.PRED_DATA_PREFIX.length());
                ensureNodeChunk(subject, writer, nodeBuffer).addProperty(key, object);
            } else if (DDotOutput.PREFIX_RELATION.equals(predicate)) {
                // `A ..prefix.. B` declares a namespace; collect into the document @context (ids left as-is)
                contextMap.put(subject, object);
            } else if (isTypeRelation(predicate)) {
                // `subject ..has type.. T` (~ rdf:type) sets a node type, not an edge. See https://ddot.it/relations.
                ensureNodeChunk(subject, writer, nodeBuffer).addType(ICjElementType.of(object));
            } else {
                attachMeta(emitEdge(writer, nodeBuffer, edgeBuffer, subject, predicate, object), edgeMeta, event.meta);
            }
        }

        if (events.isEmpty()) {
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

    /**
     * Get (creating if needed) the buffered node chunk for {@code nodeId}. Nodes are buffered rather than
     * emitted immediately so that node-level facts (label, attributes) parsed on later lines can still be
     * attached. All buffered nodes are emitted (in first-seen order) before the edges.
     */
    private ICjNodeChunkMutable ensureNodeChunk(String nodeId, ICjStream writer,
                                                Map<String, ICjNodeChunkMutable> nodeBuffer) {
        return nodeBuffer.computeIfAbsent(nodeId, id -> {
            ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
            nodeChunk.id(id);
            return nodeChunk;
        });
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
     * Route one link's metadata entries into {@code edge}'s accumulator. A typed pair
     * ({@code ,, ..key.. value}) becomes a property; the built-in {@code text} relation — which ddot.it
     * gives to free metadata text — becomes a note; an untyped pair ({@code ,, .... value}) gets ddot.it's
     * {@linkplain DdotLineSerializer#DEFAULT_PREDICATE default predicate}, which is also how it is written
     * back out.
     */
    private static void attachMeta(ICjEdgeChunkMutable edge, Map<ICjEdgeChunkMutable, LinkMeta> edgeMeta,
                                   List<DdotEvent.MetaPair> meta) {
        if (meta.isEmpty()) return;
        LinkMeta acc = edgeMeta.computeIfAbsent(edge, e -> new LinkMeta());
        for (DdotEvent.MetaPair pair : meta) {
            @Nullable String type = pair.type();
            if (type == null) acc.props.put(DdotLineSerializer.DEFAULT_PREDICATE, pair.to());
            else if (META_TEXT_RELATION.equals(type)) acc.texts.add(pair.to());
            else acc.props.put(type, pair.to());
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
        // ddot.it models a whole meta text block as ONE `text` entry; several entries can only come from
        // several `,,` constructs on the same link, and they are re-written as one block, so join them.
        if (!meta.texts.isEmpty()) {
            data.add(DDotOutput.LINK_TEXT_KEY, String.join("\n", meta.texts));
        }
        edge.dataMutable(d -> d.setJsonValue(data));
    }

    /**
     * If {@code token} is a ddot.it command, its bare command word; otherwise {@code null}. All four
     * spellings denote the same command — {@code https://ddot.it/<word>}, {@code http://ddot.it/<word>},
     * {@code ddot.it/<word>} and the {@code !!<word>} shorthand — with the scheme stripped by ddot-core's
     * {@link DdotCommands#commandToken(String)} so this reader and the ddot.it linter agree by construction.
     * <p>
     * This is <em>vocabulary</em> recognition on a slot value the parser already isolated ({@code from} or
     * {@code type}), not parsing: which commands mean something is up to the consumer, and graphinout only
     * gives meaning to {@code this} and {@code label}.
     */
    private static @Nullable String commandWord(String token) {
        String t = DdotCommands.commandToken(token);
        if (t.startsWith("ddot.it/")) return t.substring("ddot.it/".length());
        if (t.startsWith("!!")) return t.substring(2);
        return null;
    }

    /**
     * Is this predicate the "set node label" command? Accepts graphinout's own round-trip form
     * ({@code ddot:label}) and the ddot.it-native {@code label} command in any spelling. See https://ddot.it/label.
     */
    private static boolean isLabelCommand(String predicate) {
        return DDotOutput.PRED_LABEL.equals(predicate) || "label".equals(commandWord(predicate));
    }

    /** Is this subject the "current document" command {@code this} (in any spelling)? See https://ddot.it/this. */
    private static boolean isThisCommand(String subject) {
        return "this".equals(commandWord(subject));
    }

    /** Map a relation alias to its canonical name; non-aliases (incl. the empty untyped link) pass through. */
    private static String canonicalRelation(String predicate) {
        return RELATION_ALIASES.getOrDefault(predicate, predicate);
    }

    /** Is this predicate the rdf:type-like relation ({@code has type}, or aliases {@code is a}/{@code type})? */
    private static boolean isTypeRelation(String predicate) {
        return DDotOutput.TYPE_RELATION.equals(canonicalRelation(predicate));
    }

    /**
     * If the link metadata flags this object as an RDF literal, the CJ value to store under
     * {@code rdf:data} (a bare string for a plain literal, or a {@code {value,datatype|language}} envelope);
     * otherwise {@code null} (the object is an ordinary resource). See doc/spec-ddot-rdf.adoc E1.
     */
    private static @Nullable IJsonValue literalValue(String object, List<DdotEvent.MetaPair> meta, IJsonFactory jf) {
        for (DdotEvent.MetaPair pair : meta) {
            if (pair.type() == null) continue;
            switch (pair.type()) {
                case DDotOutput.MARK_PLAIN:
                    return jf.createString(object);
                case DDotOutput.MARK_DATATYPE: {
                    IJsonObjectMutable o = jf.createObjectMutable();
                    o.add(DDotOutput.LIT_VALUE, object);
                    o.add(DDotOutput.LIT_DATATYPE, pair.to());
                    return o;
                }
                case DDotOutput.MARK_LANGUAGE: {
                    IJsonObjectMutable o = jf.createObjectMutable();
                    o.add(DDotOutput.LIT_VALUE, object);
                    o.add(DDotOutput.LIT_LANGUAGE, pair.to());
                    return o;
                }
                default:
                    // not a literal marker; keep looking
            }
        }
        return null;
    }

    /** The label language from a {@code ,, ..lang.. xx} metadata entry, or {@code null}. */
    private static @Nullable String labelLanguage(List<DdotEvent.MetaPair> meta) {
        for (DdotEvent.MetaPair pair : meta) {
            if ("lang".equals(pair.type()) && !pair.to().isBlank()) return pair.to();
        }
        return null;
    }

    /** Create and buffer a directed edge {@code subject --predicate--> object} (empty predicate = untyped). */
    private ICjEdgeChunkMutable emitEdge(ICjStream writer, Map<String, ICjNodeChunkMutable> nodeBuffer,
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
