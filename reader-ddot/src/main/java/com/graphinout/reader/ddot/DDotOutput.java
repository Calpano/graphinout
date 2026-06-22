package com.graphinout.reader.ddot;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class DDotOutput {

    private static final Logger log = getLogger(DDotOutput.class);

    /**
     * Reserved DDot predicates used to carry graph-model facts that are not edges:
     * a standalone node, a node display label, and an arbitrary node attribute. The reader
     * recognises these and reconstructs node-level state instead of creating an edge.
     */
    static final String PRED_NODE = "ddot:node";
    static final String PRED_LABEL = "ddot:label";
    static final String PRED_DATA_PREFIX = "ddot:data:";

    /**
     * Per-link metadata is namespaced on the edge's CJ data so it never collides with arbitrary keys:
     * structured {@code ,, ..key.. value} entries live under {@link #LINK_PROPS_KEY}, free-text
     * {@code ,, note} entries under {@link #LINK_TEXT_KEY}.
     */
    static final String LINK_PROPS_KEY = "ddot-it:props";
    static final String LINK_TEXT_KEY = "ddot-it:text";

    /** Subject naming the current document; its triples are document-level metadata. See https://ddot.it/this. */
    static final String SUBJECT_THIS = "ddot.it/this";

    /** Object marker opening a multi-line block literal; the value is the following lines. See https://ddot.it/block. */
    static final String OBJECT_BLOCK = "ddot.it/block";

    /** RDF namespace declaration relation: {@code A ..prefix.. B} ↔ document @context {A: B}. See https://ddot.it/rdf. */
    static final String PREFIX_RELATION = "prefix";

    /** Canonical "is a type of" relation (~ rdf:type); carries a CJ node type. See https://ddot.it/relations. */
    static final String TYPE_RELATION = "has type";

    private final ICjDocument cjDoc;

    public DDotOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    private static @Nullable String firstLabelOrDesc(ICjHasData hasData, List<ICjLabelEntry> labels) {
        if (!labels.isEmpty()) {
            String val = labels.getFirst().value();
            if (val != null && !val.isEmpty()) return val;
        }
        IJsonValue json = hasData.jsonValue();
        if (json != null && json.isObject()) {
            IJsonValue desc = json.resolve(CjDataProperty.Description.cjPropertyKey);
            if (desc != null) {
                String s = desc.toXmlFragmentString().rawXml();
                if (s != null && !s.isEmpty()) return s;
            }
        }
        return null;
    }

    public String toDDot() {
        DDotDoc doc = new DDotDoc();
        cjDoc2ddotDoc(cjDoc, doc);
        return doc.toDDot();
    }

    private void cjDoc2ddotDoc(ICjDocument cjDoc, DDotDoc out) {
        @Nullable Map<String, String> context = cjDoc.context();
        boolean hasContext = context != null && !context.isEmpty();

        // The @context round-trips as `A ..prefix.. B` declarations and ids are kept verbatim (no expansion),
        // mirroring the reader (DDotReader: `A ..prefix.. B` → @context{A:B}). See https://ddot.it/rdf.
        if (hasContext) {
            context.forEach((prefix, namespace) ->
                    out.triples.add(new DDotDoc.DDotTriple(prefix, PREFIX_RELATION, namespace)));
        }

        // Document-level data round-trips as `ddot.it/this ..key.. value` triples (see https://ddot.it/this).
        // A multi-valued key (array) was authored as a repeated predicate, so emit one triple per element.
        IJsonValue docData = cjDoc.data().jsonValue();
        if (docData != null && docData.isObject()) {
            docData.asObject().forEach((key, value) -> {
                if (value != null && value.isArray()) {
                    value.asArray().forEach(el -> out.triples.add(new DDotDoc.DDotTriple(SUBJECT_THIS, key, jsonAsScalar(el))));
                } else {
                    out.triples.add(new DDotDoc.DDotTriple(SUBJECT_THIS, key, jsonAsScalar(value)));
                }
            });
        }

        Set<String> edgeNodeIds = new HashSet<>();
        cjDoc.edgesAll().forEach(e -> {
            ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
            ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
            String subject = null, object = null;
            if (inEp != null && outEp != null) {
                subject = inEp.node();
                object = outEp.node();
            } else {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() == 2) {
                    subject = eps.get(0).node();
                    object = eps.get(1).node();
                } else {
                    log.warn("Cannot represent hyper-edge in DDot");
                    return;
                }
            }
            if (subject == null || object == null) return;
            // ids are emitted verbatim (CURIEs stay CURIEs); the @context above carries the namespaces
            edgeNodeIds.add(subject);
            edgeNodeIds.add(object);
            // The predicate names the directed, typed edge. Prefer the edge type (a DDot predicate is a type);
            // fall back to the edge label. An edge with neither round-trips as a DDot "untyped link"
            // (empty predicate: "a .. .. b"). The reader restores both type and label from the predicate.
            String predicate = edgeType(e);
            if (predicate == null) predicate = firstLabelOrDesc(e, e.labelEntries());
            if (predicate == null) predicate = "";
            // Per-link metadata round-trips from the edge's namespaced CJ data (see LINK_PROPS_KEY/LINK_TEXT_KEY):
            // structured props as ",, ..key.. value", free text as ",, note".
            List<String> meta = new ArrayList<>();
            IJsonValue edgeJson = e.data().jsonValue();
            if (edgeJson != null && edgeJson.isObject()) {
                IJsonValue props = edgeJson.asObject().get(LINK_PROPS_KEY);
                if (props != null && props.isObject()) {
                    List<String> keys = new ArrayList<>(props.asObject().keys());
                    Collections.sort(keys); // deterministic order
                    for (String key : keys) {
                        meta.add(".." + key + ".. " + jsonAsScalar(props.asObject().get(key)));
                    }
                }
                IJsonValue text = edgeJson.asObject().get(LINK_TEXT_KEY);
                if (text != null) {
                    if (text.isArray()) text.asArray().forEach(v -> meta.add(jsonAsScalar(v)));
                    else if (!text.isNull()) meta.add(jsonAsScalar(text));
                }
            }
            out.triples.add(new DDotDoc.DDotTriple(subject, predicate, object, meta));
        });

        // Node-level facts that no edge can carry: standalone nodes, display labels, attributes.
        cjDoc.nodesAll().forEach(n -> {
            String id = n.id();
            if (id == null) return;
            String subject = id; // verbatim; @context carries the namespaces (no expansion)

            // node display label (carry the language tag as `,, ..lang.. xx` metadata when present)
            List<ICjLabelEntry> labelEntries = n.labelEntries();
            String label = firstLabelOrDesc(n, labelEntries);
            if (label != null && !label.isEmpty()) {
                String lang = labelEntries.isEmpty() ? null : labelEntries.getFirst().language();
                List<String> labelMeta = (lang != null && !lang.isEmpty()) ? List.of("..lang.. " + lang) : List.of();
                out.triples.add(new DDotDoc.DDotTriple(subject, PRED_LABEL, label, labelMeta));
            }

            // node types (~ rdf:type): one `subject ..has type.. T` per declared type
            n.types().forEach(t -> out.triples.add(new DDotDoc.DDotTriple(subject, TYPE_RELATION, t.type())));

            // node attributes (flat string-valued properties)
            IJsonValue json = n.data().jsonValue();
            if (json != null && json.isObject()) {
                json.asObject().forEach((key, value) -> {
                    if (CjDataProperty.Description.cjPropertyKey.equals(key)) return; // already emitted as label
                    out.triples.add(new DDotDoc.DDotTriple(subject, PRED_DATA_PREFIX + key, jsonAsScalar(value)));
                });
            }

            // a standalone node (no incident edge) needs an explicit existence marker to survive
            if (!edgeNodeIds.contains(subject)) {
                out.triples.add(new DDotDoc.DDotTriple(subject, PRED_NODE, subject));
            }
        });
    }

    private static @Nullable String edgeType(com.graphinout.base.cj.document.ICjEdge e) {
        String t = e.type();
        return (t != null && !t.isBlank()) ? t : null;
    }

    private static String jsonAsScalar(IJsonValue value) {
        if (value == null) return "";
        if (value.isString()) return value.asString();
        return value.toJsonString();
    }
}
