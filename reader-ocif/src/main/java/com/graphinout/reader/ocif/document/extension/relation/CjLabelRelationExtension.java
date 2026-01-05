package com.graphinout.reader.ocif.document.extension.relation;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * TODO add to CJ docs, maybe link from OCIF docs as example
 * <p>
 * OCIF relations have no native label support. Neither do they support rendering a resource, as they are non-visual in nature. However, CJ edges do have a label.
 * To round-trip label information meaningful in OCIF, there are two options.
 * <h2>Option A</h2>
 * Create a synthetic visual OCIF node, let the OCIF relation link to it, then create a synthetic resource to hold the label, attach it to the synthetic node, and if present, attach a CJ language extension to the synthetic resource representation.
 * <pre><code>
 * CJ:
 * edge A with label B
 *   label B with language C
 *
 * OCIF:
 * relation A with a node A1
 *   node A1 with resource A2
 *     resource A2 with representation A3
 *       representation A3 with content B and extension A4
 *         extension A4 with language C
 * </code></pre>
 * <h2>Option B</h2>
 * Store the label as a {@link CjLabelRelationExtension} on the OCIF relation directly.
 * <pre><code>
 * CJ:
 * edge A with label B
 *   label B with language C
 *
 * OCIF:
 * relation A with extension A1
 *   extension A1 with type CjLabelRelationExtension, content B and language C
 * </code></pre>
 * <p>
 * See <a href="https://calpano.github.io/connected-json/spec-cj.html">CJ spec</a>.
 */
public class CjLabelRelationExtension extends OcifExtension implements IOcifRelationExtension {

    public static final String TYPE_NAME = "@connected-json/label";
    public static final String TYPE_URI = "https://j-s-o-n.org/ocif-label/schema.json";

    public CjLabelRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull CjLabelRelationExtension of(@NonNull ICjLabel cjLabel) {
        CjLabelRelationExtension ext = new CjLabelRelationExtension();
        ext.label(cjLabel);
        return ext;
    }

    public CjLabelRelationExtension copy() {
        CjLabelRelationExtension data = new CjLabelRelationExtension();
        map().forEach(data::set);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(CjConstants.LABEL);
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    public ICjLabel label() {
        IJsonValue v = map().get(CjConstants.LABEL);
        assert v != null : "Label value cannot be null";
        if (v.isArray()) {
            return ICjLabel.fromJsonValue(v.asArray());
        } else {
            throw new IllegalArgumentException("Label value must be an array");
        }
    }

    public void label(@NonNull ICjLabel label) {
        set(CjConstants.LABEL, label().toJsonValue());
    }

    @Override
    public String toString() {
        return "CjLabelNodeExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}
