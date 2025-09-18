package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IJsonValue {

    default IJsonArray asArray() throws ClassCastException {
        return (IJsonArray) this;
    }

    default IJsonObject asObject() throws ClassCastException {
        return (IJsonObject) this;
    }

    default IJsonPrimitive asPrimitive() throws ClassCastException {
        return (IJsonPrimitive) this;
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

    boolean isArray();

    boolean isObject();

    boolean isPrimitive();

    JsonType jsonType();

    default void onProperties(BiConsumer<String, IJsonValue> key_value) {
        if (isObject()) {
            asObject().forEach(key_value);
        }
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
        List<Object> path = JsonPaths.of(jsonPath);

        // base case
        if (path.isEmpty()) consumer.accept(this);

        // take first step
        Object step = path.getFirst();
        if (step instanceof String propertyKey) {
            // try to resolve property
            if (isObject()) {
                IJsonValue value = asObject().get(propertyKey);
                if (value != null) {
                    value.resolve(path.subList(1, path.size()), consumer);
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
                        value.resolve(path.subList(1, path.size()), consumer);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid path step: " + step);
        }
    }


}
