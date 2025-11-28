package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.json.value.IJsonValue;

import org.jspecify.annotations.Nullable;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.mapOrNull;

public interface ICjHasData extends ICjElement {

    /**
     * Read existing data. To write, you need an {@link ICjHasDataMutable}.
     * @return read-only data or null
     */
    @Nullable
    ICjData data();

    /**
     * @param consumer receives only if non-null
     */
    default void data(Consumer<ICjData> consumer) {
        ICjData data = data();
        if (data != null) {
            consumer.accept(data);
        }
    }

    default void fireDataMaybe(ICjWriter cjWriter) {
        onDataValue(jsonValue -> {
            cjWriter.jsonDataStart();
            jsonValue.fire(cjWriter);
            cjWriter.jsonDataEnd();
        });
    }

    /** @return the current data JSON contents or null */
    default @Nullable IJsonValue jsonValue() {
        return mapOrNull(data(), ICjData::jsonValue);
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


}
