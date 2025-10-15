package com.graphinout.base.cj.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of CjLabel that can represent either a simple string label or a multi-lingual label according to the
 * Connected JSON specification.
 */
@Deprecated
public class CjLabelImpl implements ICjLabel {

    private static final String SIMPLE_LABEL = "*";
    private final Map<String, String> lang_label = new HashMap<>();

    /**
     * Creates a simple string label.
     */
    public static CjLabelImpl of(String label) {
        return new CjLabelImpl().add(SIMPLE_LABEL, label);
    }

    /**
     * Creates a multi-lingual label.
     */
    public static CjLabelImpl of(Map<String, String> multiLingualLabels) {
        CjLabelImpl label = new CjLabelImpl();
        multiLingualLabels.forEach(label::add);
        return label;
    }

    @Override
    public Map<String, String> asMultiLingual() {
        if (isSimple()) {
            throw new IllegalStateException("This is not a multi-lingual label");
        }
        return Collections.unmodifiableMap(lang_label);
    }

    @Override
    public String asString() {
        if (!isSimple()) {
            throw new IllegalStateException("This is not a simple string label");
        }
        return lang_label.get(SIMPLE_LABEL);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CjLabelImpl label)) return false;

        return lang_label.equals(label.lang_label);
    }

    @Override
    public int hashCode() {
        return lang_label.hashCode();
    }

    @Override
    public boolean isMultiLingual() {
        return !isSimple();
    }

    @Override
    public boolean isSimple() {
        return lang_label.containsKey(SIMPLE_LABEL);
    }

    @Override
    public String toString() {
        return "CjLabelImpl{" +
                lang_label +
                '}';
    }

    private CjLabelImpl add(String langCode, String label) {
        lang_label.put(langCode, label);
        return this;
    }

}
