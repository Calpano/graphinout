package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Mixin for CJ elements that carry an optional label (one or more, possibly localized, entries).
 */
public interface ICjHasLabel {

    default void fireLabelMaybe(ICjWriter cjWriter, boolean sort) {
        ofNullable(label()).ifPresent(l -> l.fire(cjWriter, sort));
    }

    @Nullable
    ICjLabel label();

    default List<ICjLabelEntry> labelEntries() {
        ICjLabel label = label();
        return label == null ? Collections.emptyList() : label.entries().toList();
    }


}
