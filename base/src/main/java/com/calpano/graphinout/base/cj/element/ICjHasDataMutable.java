package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData, ICjChunkMutable {

    void addData(Consumer<IMagicMutableJsonValue> consumer);

    void addDataElement(Consumer<ICjData> consumer);

}
