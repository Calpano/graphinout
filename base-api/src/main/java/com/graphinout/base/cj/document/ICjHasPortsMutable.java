package com.graphinout.base.cj.document;

import java.util.function.Consumer;

/**
 * This one is not really a chunk in {@code ICjStream}, but it makes implementation easier.
 */
public interface ICjHasPortsMutable extends ICjHasPorts, ICjChunkMutable {

    void addPort(Consumer<ICjPortMutable> consumer);

}
