package com.graphinout.base.cj.document;

import com.graphinout.base.cj.stream.ICjStream;

import java.util.function.Consumer;

/**
 * This one is not really a chunk in {@link ICjStream}, but it makes implementation easier.
 */
public interface ICjHasPortsMutable extends ICjHasPorts, ICjChunkMutable {

    void addPort(Consumer<ICjPortMutable> consumer);

}
