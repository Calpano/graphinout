package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;

import java.util.function.Consumer;

public interface ICjHasDataMutable extends ICjHasData {

    void data(Consumer<IMagicMutableJsonValue> consumer);

    void dataElement(Consumer<ICjData> consumer);

}
