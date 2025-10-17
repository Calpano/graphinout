package com.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData, ICjChunkMutable {

    ICjDataMutable data();

    /**
     * @param consumer receive current or new {@link ICjDataMutable}, never null.
     */
    void dataMutable(Consumer<ICjDataMutable> consumer);

}
