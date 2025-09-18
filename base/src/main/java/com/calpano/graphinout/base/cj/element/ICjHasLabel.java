package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public interface ICjHasLabel extends ICjElement {

    @Nullable
    ICjLabel label();

    default List<ICjLabelEntry> labelEntries() {
        ICjLabel label = label();
        return label == null ? Collections.emptyList() : label.entries().toList();
    }

}
