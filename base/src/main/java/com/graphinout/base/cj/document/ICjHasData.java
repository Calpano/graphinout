package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Mixin for CJ elements that carry a JSON data payload.
 */
public interface ICjHasData {

    /** @return this as {@link ICjElement} or throws */
    default @NonNull ICjElement asCjElement() throws ClassCastException {
        return (ICjElement) this;
    }

    /**
     * Read existing data. To write, you need an {@link ICjHasDataMutable}.
     *
     * @return read-only data or null
     */
    @NonNull
    ICjData data();

    /**
     * @param consumer receives only if non-null
     */
    default void data(Consumer<ICjData> consumer) {
        consumer.accept(data());
    }

    default @Nullable ICjData dataIfNotEmpty() {
        if (data().isEmpty())
            return null;
        return data();
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
        return data().jsonValue();
    }

    /**
     * @param consumer is called if data is present.
     */
    default void onDataValue(Consumer<IJsonValue> consumer) {
        IJsonValue value = data().jsonValue();
        if (value != null) {
            consumer.accept(value);
        }
    }


}
