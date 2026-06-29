package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An {@link ICjStream} that sits in front of a real writer's stream, anonymizes every chunk as it flows through, and
 * forwards the redacted result to that {@code delegate} immediately — without buffering the graph.
 *
 * <p>It applies the same rules as {@link CjDocumentAnonymizer} (labels/types/string values run through
 * {@link Anonymizer}; numbers zeroed; ids remapped to {@code node1/edge1/graph1/port1/…}; data object keys to
 * {@code key1/…}; directions, languages, {@code @context} and {@code connectedJson} kept), but in a single streaming
 * pass: each identifier and data key is interned the first time it is seen and reused thereafter, so links stay intact
 * and a forward-referenced node id (an edge endpoint seen before the node is declared) is minted on first contact and
 * the eventual node declaration reuses it.
 *
 * <p><b>Memory.</b> Unlike a document-level anonymizer, this holds <em>no</em> graph — only five
 * {@code original → anonymized} string maps: one entry per <em>distinct</em> node id, edge id, graph id, port id and
 * data-object key. Peak heap is therefore
 * <pre>  O(|distinct node ids| + |distinct edge ids| + |distinct graph ids| + |distinct port ids| + |distinct data keys|)</pre>
 * counted in (original+synthetic) string bytes, independent of the number of nodes/edges streamed and of the size of
 * any label, type or data <em>value</em> (values are redacted and forwarded, never retained). For a graph with N nodes
 * and E edges that is at most N+E id entries plus the distinct-key count — typically a few percent of the
 * "one full document" a buffering anonymizer would need.
 *
 * <pre>{@code
 * ICjStream out = writer.createCjStream(sink);
 * reader.read(input, new AnonymizingCjStream(out));
 * }</pre>
 */
public class AnonymizingCjStream implements ICjStream {

    private final ICjStream delegate;
    private final IJsonFactory jf = IJsonFactory.INSTANCE;

    // The ONLY retained state: consistent original -> anonymized maps (no graph content is buffered).
    private final Map<String, String> nodeIds = new HashMap<>();
    private final Map<String, String> edgeIds = new HashMap<>();
    private final Map<String, String> graphIds = new HashMap<>();
    private final Map<String, String> portIds = new HashMap<>();
    private final Map<String, String> keys = new HashMap<>();

    public AnonymizingCjStream(ICjStream delegate) {
        this.delegate = delegate;
    }

    // ---- stream lifecycle: anonymize each chunk and forward immediately --------------------------------------------

    @Override
    public void documentStart(ICjDocumentChunk document) {
        ICjDocumentChunkMutable t = delegate.createDocumentChunk();
        if (document.context() != null) {
            t.context(document.context());
        }
        if (document.connectedJson() != null) {
            t.connectedJson(document.connectedJson());
        }
        anonData(document.data(), t);
        delegate.documentStart(t);
    }

    @Override
    public void documentEnd() {
        delegate.documentEnd();
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        ICjGraphChunkMutable t = delegate.createGraphChunk();
        if (graph.id() != null) {
            t.id(intern(graphIds, graph.id(), "graph"));
        }
        anonLabel(graph.label(), t);
        anonData(graph.data(), t);
        delegate.graphStart(t);
    }

    @Override
    public void graphEnd() {
        delegate.graphEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        ICjNodeChunkMutable t = delegate.createNodeChunk();
        if (node.id() != null) {
            t.id(intern(nodeIds, node.id(), "node"));
        }
        anonLabel(node.label(), t);
        node.types().forEach(ty -> t.addType(ICjElementType.of(Anonymizer.text(ty.type()))));
        node.ports().forEach(p -> t.addPort(tp -> anonPort(p, tp)));
        anonData(node.data(), t);
        delegate.nodeStart(t);
    }

    @Override
    public void nodeEnd() {
        delegate.nodeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        ICjEdgeChunkMutable t = delegate.createEdgeChunk();
        if (edge.id() != null) {
            t.id(intern(edgeIds, edge.id(), "edge"));
        }
        anonLabel(edge.label(), t);
        if (edge.edgeType() != null) {
            t.edgeType(ICjElementType.of(Anonymizer.text(edge.edgeType().type())));
        }
        edge.endpoints().forEach(ep -> t.addEndpoint(te -> {
            if (ep.node() != null) {
                te.node(intern(nodeIds, ep.node(), "node"));
            }
            if (ep.port() != null) {
                te.port(intern(portIds, ep.port(), "port"));
            }
            if (ep.direction() != null) {
                te.direction(ep.direction());
            }
            if (ep.type() != null) {
                te.type(Anonymizer.text(ep.type()));
            }
            anonData(ep.data(), te);
        }));
        anonData(edge.data(), t);
        delegate.edgeStart(t);
    }

    @Override
    public void edgeEnd() {
        delegate.edgeEnd();
    }

    // ---- per-chunk anonymization helpers ---------------------------------------------------------------------------

    private void anonPort(ICjPort source, ICjPortMutable t) {
        if (source.id() != null) {
            t.id(intern(portIds, source.id(), "port"));
        }
        anonLabel(source.label(), t);
        anonData(source.data(), t);
        source.ports().forEach(p -> t.addPort(tp -> anonPort(p, tp)));
    }

    private void anonLabel(@Nullable ICjLabel source, ICjHasLabelMutable target) {
        if (source == null) {
            return;
        }
        target.labelMutable(lm -> {
            source.entries().forEach(en -> lm.addEntry(em -> {
                em.value(Anonymizer.text(en.value()));
                if (en.language() != null) {
                    em.language(en.language());
                }
                anonData(en.data(), em);
            }));
            anonData(source.data(), lm);
        });
    }

    private void anonData(@Nullable ICjData source, ICjHasDataMutable target) {
        if (source == null || source.isEmpty()) {
            return;
        }
        IJsonValue anon = anonJson(source.jsonValue());
        if (anon != null) {
            target.dataMutable(d -> d.setJsonValue(anon));
        }
    }

    private @Nullable IJsonValue anonJson(@Nullable IJsonValue v) {
        if (v == null) {
            return null;
        }
        if (v.isObject()) {
            IJsonObjectMutable o = jf.createObjectMutable();
            // sort keys so the synthetic key numbering is deterministic within an object
            v.asObject().keys().stream().sorted()
                    .forEach(k -> o.addProperty(intern(keys, k, "key"), anonJson(v.asObject().get(k))));
            return o;
        }
        if (v.isArray()) {
            IJsonArrayMutable a = jf.createArrayMutable();
            v.asArray().forEach(x -> a.add(anonJson(x)));
            return a;
        }
        if (v.isString()) {
            return jf.createString(Anonymizer.text(v.asString()));
        }
        if (v.isNumber()) {
            return jf.createInteger(0); // zero out numeric content
        }
        return v; // boolean, null: keep
    }

    /** Map {@code original} to a stable {@code prefix + N} id, minting it on first encounter. */
    private static String intern(Map<String, String> map, String original, String prefix) {
        return map.computeIfAbsent(original, k -> prefix + (map.size() + 1));
    }

    // ---- factory + content-error / locator plumbing: forward to the real writer ------------------------------------

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return delegate.createDocumentChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return delegate.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return delegate.createNodeChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return delegate.createEdgeChunk();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return delegate.jsonFactory();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        delegate.setContentErrorHandler(errorHandler);
    }

    @Override
    public @Nullable Consumer<ContentError> contentErrorHandler() {
        return delegate.contentErrorHandler();
    }

    @Override
    public @Nullable Locator locator() {
        return delegate.locator();
    }

    @Override
    public void setLocator(Locator locator) {
        delegate.setLocator(locator);
    }
}
