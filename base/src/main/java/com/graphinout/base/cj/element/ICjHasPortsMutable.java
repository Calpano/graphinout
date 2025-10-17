package com.graphinout.base.cj.element;

import com.graphinout.base.cj.stream.api.ICjStream;

import java.util.function.Consumer;

/**
 * This one is not really a chunk in {@link ICjStream}, but it makes implementation easier.
 */
public interface ICjHasPortsMutable extends ICjHasPorts, ICjChunkMutable {

    void addPort(Consumer<ICjPortMutable> consumer);

}
