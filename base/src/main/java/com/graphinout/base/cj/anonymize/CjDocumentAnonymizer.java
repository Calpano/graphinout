package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjLabelMutable;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Anonymizes a whole {@link ICjDocument} so copyrightable / private material can be published while the
 * graph structure is preserved. Works at the document level (not streaming) so it can index every
 * identifier and JSON key first, then remap them consistently.
 *
 * <p>What is changed:
 * <ul>
 *   <li><b>Labels</b> (node, edge, graph, port, label entries) — text run through {@link Anonymizer}
 *       (letters &rarr; X/x, digits &rarr; 0, spacing/punctuation kept).</li>
 *   <li><b>Element types</b> (node types, edge type, endpoint type) — anonymized as text; equal types
 *       stay equal (the rule is deterministic), so the structure is retained.</li>
 *   <li><b>Identifiers</b> — node ids &rarr; {@code node1, node2, …}; edge ids &rarr; {@code edge1, …};
 *       graph ids &rarr; {@code graph1, …}; port ids &rarr; {@code port1, …}. The same map is applied to
 *       every reference (edge endpoints' {@code node}/{@code port}), so links stay intact.</li>
 *   <li><b>Data / metadata JSON</b> — object keys are indexed across the whole document and remapped to
 *       {@code key1, key2, …} (consistent, collision-free); string values are anonymized; numbers become
 *       {@code 0}; booleans and nulls are kept.</li>
 * </ul>
 *
 * <p>What is kept: language tags, endpoint directions, the document {@code @context} and
 * {@code connectedJson} metadata, and all non-letter/digit characters.
 */
public final class CjDocumentAnonymizer {

    private final IJsonFactory jf = IJsonFactory.INSTANCE;
    private final Map<String, String> nodeIds = new LinkedHashMap<>();
    private final Map<String, String> edgeIds = new LinkedHashMap<>();
    private final Map<String, String> graphIds = new LinkedHashMap<>();
    private final Map<String, String> portIds = new LinkedHashMap<>();
    private final Map<String, String> keys = new LinkedHashMap<>();

    /** Anonymize {@code source}, returning a new, independent document. */
    public static ICjDocument anonymize(ICjDocument source) {
        return new CjDocumentAnonymizer().run(source);
    }

    private ICjDocument run(ICjDocument source) {
        // Pass 1: index every identifier and JSON key so remaps are consistent and collision-free.
        indexData(source.data());
        source.graphs().forEach(this::indexGraph);

        // Pass 2: rebuild a fresh document applying the maps.
        CjDocumentElement target = new CjDocumentElement();
        if (source.context() != null) target.context(source.context());
        if (source.connectedJson() != null) target.connectedJson(source.connectedJson().copyMutable());
        setData(source.data(), target);
        source.graphs().forEach(g -> target.addGraph(tg -> buildGraph(g, tg)));
        return target;
    }

    // ----------------------------------------------------------------- pass 1: indexing

    private void indexGraph(ICjGraph g) {
        if (g.id() != null) intern(graphIds, g.id(), "graph");
        indexData(g.data());
        indexLabel(g.label());
        g.nodes().forEach(this::indexNode);
        g.edges().forEach(this::indexEdge);
        g.graphs().forEach(this::indexGraph);
    }

    private void indexNode(ICjNode n) {
        if (n.id() != null) intern(nodeIds, n.id(), "node");
        indexData(n.data());
        indexLabel(n.label());
        n.ports().forEach(this::indexPort);
        n.graphs().forEach(this::indexGraph);
    }

    private void indexPort(ICjPort p) {
        if (p.id() != null) intern(portIds, p.id(), "port");
        indexData(p.data());
        indexLabel(p.label());
        p.ports().forEach(this::indexPort);
    }

    private void indexEdge(ICjEdge e) {
        if (e.id() != null) intern(edgeIds, e.id(), "edge");
        indexData(e.data());
        indexLabel(e.label());
        e.endpoints().forEach(ep -> {
            if (ep.node() != null) intern(nodeIds, ep.node(), "node");
            if (ep.port() != null) intern(portIds, ep.port(), "port");
            indexData(ep.data());
        });
        e.graphs().forEach(this::indexGraph);
    }

    private void indexLabel(@Nullable ICjLabel label) {
        if (label == null) return;
        indexData(label.data());
        label.entries().forEach(en -> indexData(en.data()));
    }

    private void indexData(@Nullable ICjData data) {
        if (data != null && !data.isEmpty()) indexKeys(data.jsonValue());
    }

    private void indexKeys(@Nullable IJsonValue v) {
        if (v == null) return;
        if (v.isObject()) {
            // sorted so synthetic key numbering (key1, key2, …) is deterministic and reproducible
            v.asObject().keys().stream().sorted().forEach(k -> {
                intern(keys, k, "key");
                indexKeys(v.asObject().get(k));
            });
        } else if (v.isArray()) {
            v.asArray().forEach(this::indexKeys);
        }
    }

    private static void intern(Map<String, String> map, String original, String prefix) {
        map.computeIfAbsent(original, k -> prefix + (map.size() + 1));
    }

    // ----------------------------------------------------------------- pass 2: rebuilding

    private void buildGraph(ICjGraph s, ICjGraphMutable t) {
        if (s.id() != null) t.id(graphIds.get(s.id()));
        buildLabel(s.label(), t::setLabel);
        setData(s.data(), t);
        s.nodes().forEach(n -> t.addNode(tn -> buildNode(n, tn)));
        s.edges().forEach(e -> t.addEdge(te -> buildEdge(e, te)));
        s.graphs().forEach(g -> t.addGraph(tg -> buildGraph(g, tg)));
    }

    private void buildNode(ICjNode s, ICjNodeMutable t) {
        if (s.id() != null) t.id(nodeIds.get(s.id()));
        buildLabel(s.label(), t::setLabel);
        s.types().forEach(ty -> t.addType(ICjElementType.of(Anonymizer.text(ty.type()))));
        s.ports().forEach(p -> t.addPort(tp -> buildPort(p, tp)));
        setData(s.data(), t);
        s.graphs().forEach(g -> t.addGraph(tg -> buildGraph(g, tg)));
    }

    private void buildPort(ICjPort s, ICjPortMutable t) {
        if (s.id() != null) t.id(portIds.get(s.id()));
        buildLabel(s.label(), t::setLabel);
        setData(s.data(), t);
        s.ports().forEach(p -> t.addPort(tp -> buildPort(p, tp)));
    }

    private void buildEdge(ICjEdge s, com.graphinout.base.cj.document.ICjEdgeMutable t) {
        if (s.id() != null) t.id(edgeIds.get(s.id()));
        buildLabel(s.label(), t::setLabel);
        if (s.edgeType() != null) t.edgeType(ICjElementType.of(Anonymizer.text(s.edgeType().type())));
        s.endpoints().forEach(ep -> t.addEndpoint(te -> {
            if (ep.node() != null) te.node(nodeIds.getOrDefault(ep.node(), ep.node()));
            if (ep.port() != null) te.port(portIds.getOrDefault(ep.port(), ep.port()));
            if (ep.direction() != null) te.direction(ep.direction());
            if (ep.type() != null) te.type(Anonymizer.text(ep.type()));
            setData(ep.data(), te);
        }));
        setData(s.data(), t);
        s.graphs().forEach(g -> t.addGraph(tg -> buildGraph(g, tg)));
    }

    private void buildLabel(@Nullable ICjLabel s, java.util.function.Consumer<java.util.function.Consumer<ICjLabelMutable>> setter) {
        if (s == null) return;
        setter.accept(lm -> {
            s.entries().forEach(en -> lm.addEntry(em -> {
                em.value(Anonymizer.text(en.value()));
                if (en.language() != null) em.language(en.language());
                setData(en.data(), em);
            }));
            setData(s.data(), lm);
        });
    }

    private void setData(@Nullable ICjData source, ICjHasDataMutable target) {
        if (source == null || source.isEmpty()) return;
        IJsonValue anon = anonymizeJson(source.jsonValue());
        if (anon != null) target.dataMutable(d -> d.setJsonValue(anon));
    }

    private @Nullable IJsonValue anonymizeJson(@Nullable IJsonValue v) {
        if (v == null) return null;
        if (v.isObject()) {
            IJsonObjectMutable o = jf.createObjectMutable();
            v.asObject().keys().stream().sorted().forEach(k ->
                    o.addProperty(keys.getOrDefault(k, k), anonymizeJson(v.asObject().get(k))));
            return o;
        }
        if (v.isArray()) {
            IJsonArrayMutable a = jf.createArrayMutable();
            v.asArray().forEach(x -> a.add(anonymizeJson(x)));
            return a;
        }
        if (v.isString()) return jf.createString(Anonymizer.text(v.asString()));
        if (v.isNumber()) return jf.createInteger(0); // zero out numeric content
        return v; // boolean, null: keep
    }
}
