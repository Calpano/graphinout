package com.graphinout.foundation.json.value;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.path.IJsonNavigationPath;
import com.graphinout.foundation.json.path.IJsonObjectNavigationStep;
import com.graphinout.foundation.json.stream.JsonWriter;

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

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        forEach((key, value) -> //
        {
            IJsonNavigationPath path2 = prefix.withAppend(IJsonObjectNavigationStep.of(key));
            if (value.isPrimitive()) {
                // send out
                path_primitive.accept(path2, value.asPrimitive());
            } else {
                // RECURSE
                value.forEachLeaf(path2, path_primitive);
            }
        });
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
