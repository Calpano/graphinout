package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrNull;

public interface ICjHasData extends ICjElement {

    @Nullable
    ICjData data();

    default void data(Consumer<ICjData> consumer) {
        ICjData data = data();
        if (data != null) {
            consumer.accept(data);
        }
    }

    /** @return the current data JSON contents or null */
    default @Nullable IJsonValue jsonValue() {
        return mapOrNull( data(), ICjData::jsonValue);
    }

    /**
     * @param consumer is called if data is present.
     */
    default void onDataValue(Consumer<IJsonValue> consumer) {
        ICjData data = data();
        if (data != null) {
            IJsonValue value = data.jsonValue();
            if (value != null) {
                consumer.accept(value);
            }
        }
    }

    default void fireDataMaybe(ICjWriter cjWriter) {
        onDataValue(jsonValue->{
            cjWriter.jsonDataStart();
            jsonValue.fire(cjWriter);
            cjWriter.jsonDataEnd();
        });
    }



}
