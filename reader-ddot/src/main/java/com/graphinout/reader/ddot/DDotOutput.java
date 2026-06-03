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

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class DDotOutput {

    public static final String DEFAULT_PREDICATE = "rel";
    private static final Logger log = getLogger(DDotOutput.class);
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
            String predicate = firstLabelOrDesc(e, e.labelEntries());
            if (predicate == null || predicate.isEmpty()) predicate = DEFAULT_PREDICATE;
            out.triples.add(new DDotDoc.DDotTriple(subject, predicate, object));
        });
    }
}
