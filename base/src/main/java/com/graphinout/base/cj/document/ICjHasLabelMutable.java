package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjHasLabelMutable extends ICjHasLabel {

    default void addLabel(String label, String language) {
        labelMutable().addEntry(entry -> {
            entry.value(label);
            entry.language(language);
        });
    }

    default void addLabelWithoutLanguage(String label) {
        labelMutable().addEntry(entry -> entry.value(label));
    }

    ICjLabelMutable labelMutable();

    /** at most one per element */
    void setLabel(Consumer<ICjLabelMutable> label);

}
