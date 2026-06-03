package com.graphinout.reader.d2;

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

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/** Converts a {@link ICjDocument} to D2 text. */
public class CjDocument2D2 {

    private static final Logger log = getLogger(CjDocument2D2.class);
    private final ICjDocument cjDoc;

    public CjDocument2D2(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    static @Nullable String firstLabelOrDesc(ICjHasData hasData, List<ICjLabelEntry> labels) {
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

    public String toD2() {
        D2Doc out = new D2Doc();
        cjDoc2d2Doc(cjDoc, out);
        return out.toD2();
    }

    private void cjDoc2d2Doc(ICjDocument cjDoc, D2Doc out) {
        @Nullable Map<String, String> context = cjDoc.context();
        boolean hasContext = context != null && !context.isEmpty();
        cjDoc.nodesAllIncludingImplied().forEach(cjNode -> {
            String id = hasContext ? cjNode.uri() : cjNode.id();
            String label = firstLabelOrDesc(cjNode, cjNode.labelEntries());
            out.nodes.add(new D2Doc.D2Node(id, label));
        });
        cjDoc.edgesAll().forEach(e -> {
            ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
            ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
            String source = null, target = null;
            if (inEp != null && outEp != null) {
                source = inEp.node();
                target = outEp.node();
            } else {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() == 2) {
                    source = eps.get(0).node();
                    target = eps.get(1).node();
                } else {
                    log.warn("Cannot represent hyper-edge in D2");
                    return;
                }
            }
            if (source == null || target == null) return;
            if (hasContext) {
                source = CjUris.expandId(context, source);
                target = CjUris.expandId(context, target);
            }
            String label = firstLabelOrDesc(e, e.labelEntries());
            out.edges.add(new D2Doc.D2Edge(source, target, label));
        });
    }
}
