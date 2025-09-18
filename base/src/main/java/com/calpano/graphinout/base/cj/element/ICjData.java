package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface ICjData extends ICjElement {

    default @Nullable IJsonValue jsonValue() {
        IMagicMutableJsonValue mutableJsonValue = jsonValueMutable();
        if (mutableJsonValue == null) {
            return null;
        }
        if (mutableJsonValue.jsonType() == JsonType.Undefined)
            return null;
        return mutableJsonValue;
    }

    @Nullable
    IMagicMutableJsonValue jsonValueMutable();

    default void resolve(Object jsonPath, Consumer<IJsonValue> consumer) {
        IJsonValue value = jsonValue();
        if (value != null) {
            value.resolve(jsonPath, consumer);
        }
    }

}
