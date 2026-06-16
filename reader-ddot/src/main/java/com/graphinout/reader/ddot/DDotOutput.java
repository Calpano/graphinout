package com.graphinout.reader.ddot;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjUris;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

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
            if (hasContext) {
                subject = CjUris.expandId(context, subject);
                object = CjUris.expandId(context, object);
            }
            edgeNodeIds.add(subject);
            edgeNodeIds.add(object);
            // The predicate names the directed, typed edge. Prefer the edge type (a DDot predicate is a type);
            // fall back to the edge label. An edge with neither round-trips as a DDot "untyped link"
            // (empty predicate: "a .. .. b"). The reader restores both type and label from the predicate.
            String predicate = edgeType(e);
            if (predicate == null) predicate = firstLabelOrDesc(e, e.labelEntries());
            if (predicate == null) predicate = "";
            out.triples.add(new DDotDoc.DDotTriple(subject, predicate, object));
        });

        // Node-level facts that no edge can carry: standalone nodes, display labels, attributes.
        cjDoc.nodesAll().forEach(n -> {
            String id = n.id();
            if (id == null) return;
            String subject = hasContext ? CjUris.expandId(context, id) : id;

            // node display label
            String label = firstLabelOrDesc(n, n.labelEntries());
            if (label != null && !label.isEmpty()) {
                out.triples.add(new DDotDoc.DDotTriple(subject, PRED_LABEL, label));
            }

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
