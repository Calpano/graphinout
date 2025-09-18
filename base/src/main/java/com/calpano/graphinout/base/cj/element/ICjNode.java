package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface ICjNode extends ICjHasId, ICjHasGraphs {

    @Nullable
    ICjData data();

    @Nullable
    ICjLabel label();

    default List<ICjLabelEntry> labelEntries() {
        ICjLabel label = label();
        return label == null ? Collections.emptyList() : label.entries().toList();
    }

    Stream<ICjPort> ports();

}
