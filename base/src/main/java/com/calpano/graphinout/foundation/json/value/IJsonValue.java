package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.util.path.IListLike;
import com.calpano.graphinout.foundation.util.path.IMapLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IJsonValue {

    default IJsonArray asArray() throws ClassCastException {
        return (IJsonArray) this;
    }

    default Boolean asBoolean() {
        return asPrimitive().castTo(Boolean.class);
    }

    default IJsonContainer asContainer() throws ClassCastException {
        return (IJsonContainer) this;
    }

    default IListLike asListLike() {
        if (isArray()) {
            IJsonArray arr = asArray();
            return IListLike.of(arr::size, arr::get);
        } else
            return IListLike.EMPTY;
    }

    default IMapLike asMapLike() {
        if (isObject()) {
            IJsonObject obj = asObject();
            return IMapLike.of(() -> obj.keys().stream().sorted().toList(), obj::get);
        } else {
            return IMapLike.EMPTY;
        }
    }

    default IJsonValueMutable asMutable() throws ClassCastException {
        return (IJsonValueMutable) this;
    }

    default Number asNumber() {
        return asPrimitive().castTo(Number.class);
    }

    default IJsonObject asObject() throws ClassCastException {
        return (IJsonObject) this;
    }

    default IJsonPrimitive asPrimitive() throws ClassCastException {
        return (IJsonPrimitive) this;
    }

    default String asString() {
        return asPrimitive().castTo(String.class);
    }

    /** the underlying implementation */
    Object base();

    IJsonFactory factory();

    default void fire(JsonWriter jsonWriter) {
        if (isArray()) {
            jsonWriter.arrayStart();
            asArray().forEach(member -> member.fire(jsonWriter));
            jsonWriter.arrayEnd();
        } else if (isObject()) {
            jsonWriter.objectStart();
            asObject().forEach((key, value) -> {
                jsonWriter.onKey(key);
                value.fire(jsonWriter);
            });
            jsonWriter.objectEnd();
        } else {
            assert isPrimitive();
            IJsonPrimitive p = asPrimitive();
            p.fire(jsonWriter);
        }
    }

    default boolean isAppendable() {
        return (this instanceof IJsonObjectAppendable || this instanceof IJsonArrayAppendable);
    }

    boolean isArray();

    default boolean isContainer() {
        return isArray() || isObject();
    }

    /** Mutable is stronger than Appendable */
    default boolean isMutable() {
        return (this instanceof IJsonObjectMutable || this instanceof IJsonArrayMutable);
    }

    boolean isObject();

    boolean isPrimitive();

    JsonType jsonType();

    default void onProperties(BiConsumer<String, IJsonValue> key_value) {
        if (isObject()) {
            asObject().forEach(key_value);
        }
    }

    default @Nullable IJsonValue resolve(Object jsonPath) {
        List<Object> path = JsonPaths.of(jsonPath);

        // base case
        if (path.isEmpty()) return this;

        // take first step
        Object step = path.getFirst();
        if (step instanceof String propertyKey) {
            // try to resolve property
            if (isObject()) {
                IJsonValue value = asObject().get(propertyKey);
                if (value != null) {
                    return value.resolve(path.subList(1, path.size()));
                }
            } else {
                throw new IllegalArgumentException("Cannot resolve property on non-object value");
            }
        } else if (step instanceof Integer index) {
            // try to resolve index
            if (isArray()) {
                IJsonArray array = asArray();
                if (index < array.size()) {
                    IJsonValue value = array.get(index);
                    if (value != null) {
                        return value.resolve(path.subList(1, path.size()));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid path step: " + step);
        }

        return null;
    }

    /**
     * Resolve the path on this value.
     *
     * @param jsonPath may only contain String or Integer steps. Use {@link JsonPaths} to create a path.
     * @param consumer is called on a value if found.
     * @throws IllegalArgumentException if the path tries to resolve a property in an array or an index in an object.
     *                                  Other cases of not found result in an empty consumer.
     */
    default void resolve(Object jsonPath, Consumer<IJsonValue> consumer) throws IllegalArgumentException {
        IJsonValue value = resolve(jsonPath);
        if (value != null) consumer.accept(value);
    }

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        fire(w);
        return w.jsonString();
    }

}
