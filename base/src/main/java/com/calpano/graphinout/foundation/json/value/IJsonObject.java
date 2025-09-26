package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface IJsonObject extends IJsonContainer {

    @Override
    default void fire(JsonWriter jsonWriter) {
        jsonWriter.objectStart();
        keys().forEach(key -> {
            jsonWriter.onKey(key);
            IJsonValue value = get_(key);
            value.fire(jsonWriter);
        });
        jsonWriter.objectEnd();
    }

    default void forEach(BiConsumer<String, IJsonValue> key_value) {
        keys().forEach(key -> key_value.accept(key, get_(key)));
    }

    @Nullable
    IJsonValue get(String key);

    @Nonnull
    default IJsonValue get_(String key) {
        return Objects.requireNonNull(get(key));
    }

    default boolean hasProperty(String propertyStep) {
        return keys().contains(propertyStep);
    }

    default boolean isArray() {return false;}

    default boolean isObject() {return true;}

    default boolean isPrimitive() {return false;}

    default JsonType jsonType() {
        return JsonType.Object;
    }

    Set<String> keys();

    default Stream<Map.Entry<String, IJsonValue>> properties() {
        return keys().stream().map(key -> new AbstractMap.SimpleImmutableEntry<>(key, get_(key)));
    }

    default int size() {
        return keys().size();
    }

    /** includes potentially null values */
    default Stream<IJsonValue> values() {
        return keys().stream().map(this::get_);
    }

}
