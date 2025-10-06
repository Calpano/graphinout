package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData, ICjChunkMutable {

    /**
     * @param consumer gets the new or existing magic mutable IJsonValue
     */
    void addData(Consumer<IMagicMutableJsonValue> consumer);

    /**
     * @param consumer get the new or existing ICjData
     */
    void addDataElement(Consumer<ICjData> consumer);

}
