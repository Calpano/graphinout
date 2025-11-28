package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

public interface ICjHasLabel extends ICjElement {

    default void fireLabelMaybe(ICjWriter cjWriter) {
        ofNullable(label()).ifPresent(l -> l.fire(cjWriter));
    }

    @Nullable
    ICjLabel label();

    default List<ICjLabelEntry> labelEntries() {
        ICjLabel label = label();
        return label == null ? Collections.emptyList() : label.entries().toList();
    }


}
