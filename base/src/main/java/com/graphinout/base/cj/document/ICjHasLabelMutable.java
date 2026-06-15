package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * Mutable variant of {@link ICjHasLabel}: allows setting the label.
 */
public interface ICjHasLabelMutable extends ICjHasLabel {

    default void addLabel(String label, @Nullable String language) {
        labelMutable().addEntry(entry -> {
            entry.value(label);
            ifPresentAccept(language, entry::language);
        });
    }

    default void addLabelWithoutLanguage(String label) {
        labelMutable().addEntry(entry -> entry.value(label));
    }

    @NonNull ICjLabelMutable labelMutable();

    default void labelMutable(Consumer<ICjLabelMutable> labelMutable) {
        labelMutable.accept(labelMutable());
    }

    /** at most one per element */
    void setLabel(Consumer<ICjLabelMutable> label);

   void removeLabel();

}
