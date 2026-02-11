package com.graphinout.reader.tgf;

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

public class TgfOutput {

    private static final Logger log = getLogger(TgfOutput.class);
    private final ICjDocument cjDoc;

    public TgfOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    private static String firstLabelOrDesc(ICjHasData hasData, List<ICjLabelEntry> labels) {
        // prefer label if present
        if (!labels.isEmpty()) {
            String val = labels.getFirst().value();
            if (val != null && !val.isEmpty()) return val;
        }
        // fallback to CJ data description
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

    public String toTgf() {
        TgfDoc tgfDoc = new TgfDoc();
        cjDoc2tgfDoc(cjDoc, tgfDoc);
        return tgfDoc.toTgf();
    }

    private void cjDoc2tgfDoc(ICjDocument cjDoc, TgfDoc tgfDoc) {
        @Nullable Map<String, String> context = cjDoc.context();
        boolean hasContext = context != null && !context.isEmpty();
        cjDoc.nodesAllIncludingImplied().forEach(cjNode -> {
            String id = hasContext ? cjNode.uri() : cjNode.id();
            String text = firstLabelOrDesc(cjNode, cjNode.labelEntries());
            tgfDoc.nodes.add(new TgfDoc.TgfNode(id, text));
        });
        cjDoc.edgesAll().forEach(e -> {
            ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
            ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
            String n1 = null, n2 = null;
            if (outEp != null && inEp != null) {
                n1 = inEp.node();
                n2 = outEp.node();
            } else {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() == 2) {
                    n1 = eps.get(0).node();
                    n2 = eps.get(1).node();
                } else {
                    log.warn("Cannot represent hyper-edge in TGF");
                }
            }
            if (n1 != null && n2 != null) {
                if (hasContext) {
                    n1 = CjUris.expandId(context, n1);
                    n2 = CjUris.expandId(context, n2);
                }
                tgfDoc.edges.add(new TgfDoc.TgfEdge(n1, n2, firstLabelOrDesc(e, e.labelEntries())));
            }
        });
    }

}
