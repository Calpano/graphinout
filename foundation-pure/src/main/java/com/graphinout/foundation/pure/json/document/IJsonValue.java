package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.collections.IListLike;
import com.graphinout.foundation.pure.collections.IMapLike;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.JSON;
import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.pure.json.path.IJsonNavigationPath;
import com.graphinout.foundation.pure.json.path.JsonPaths;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "SequencedCollectionMethodCanBeUsed", "PatternVariableCanBeUsed"})
public interface IJsonValue {

    static boolean isPrimitive(@Nullable IJsonValue value) {
        if (value == null)
            return true;
        return value.isPrimitive();
    }

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
            return IMapLike.of(() -> Java9.Stream.toList(obj.keys().stream().sorted()), obj::get);
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

    default @NonNull IJsonObject asObject() throws ClassCastException {
        return (IJsonObject) this;
    }

    default @Nullable IJsonObject asObjectOrNull() {
        return isObject() ? asObject() : null;
    }

    default IJsonPrimitive asPrimitive() throws ClassCastException {
        return (IJsonPrimitive) this;
    }

    /** Does not convert boolean or number to string. Just type-casting here. */
    default String asString() {
        return asPrimitive().castTo(String.class);
    }

    /** the underlying implementation */
    Object base();

    IJsonFactory factory();

    /**
     * Fire this value (recursively) to the given writer.
     *
     * @param jsonWriter
     */
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

    default void forEachLeaf(BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        forEachLeaf(IJsonNavigationPath.EMPTY, path_primitive);
    }

    void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive);

    /**
     * Resolve a JSON path
     *
     * @param path
     * @return
     */
    default @Nullable IJsonValue get(List<IJsonContainerNavigationStep> path) {
        if (path.isEmpty()) return this;

        // resolve first step
        IJsonContainerNavigationStep step = path.get(0);
        switch (step.containerType()) {
            case Array:
                if (isArray()) {
                    IJsonValue child = asArray().get(step.asArrayStep().index());
                    if (child == null)
                        return null;
                    return child.get(path.subList(1, path.size()));
                }
            case Object:
                if (isObject()) {
                    IJsonValue child = asObject().get(step.asObjectStep().propertyKey());
                    if (child == null)
                        return null;
                    return child.get(path.subList(1, path.size()));
                }
        }
        return null;
    }

    default boolean has(List<IJsonContainerNavigationStep> path) {
        if (path.isEmpty()) return true;

        // resolve first step
        IJsonContainerNavigationStep step = path.get(0);
        switch (step.containerType()) {
            case Array:
                if (isArray()) {
                    IJsonValue child = asArray().get(step.asArrayStep().index());
                    return child != null && child.has(path.subList(1, path.size()));
                }
            case Object:
                if (isObject()) {
                    IJsonValue child = asObject().get(step.asObjectStep().propertyKey());
                    return child != null && child.has(path.subList(1, path.size()));
                }
        }
        return false;
    }

    @SuppressWarnings("unused")
    default boolean isAppendable() {
        return (this instanceof IJsonObjectAppendable || this instanceof IJsonArrayAppendable);
    }

    boolean isArray();

    default boolean isContainer() {
        return isArray() || isObject();
    }

    /** Mutable is stronger than Appendable */
    @SuppressWarnings("unused")
    default boolean isMutable() {
        return (this instanceof IJsonObjectMutable || this instanceof IJsonArrayMutable);
    }

    /** only true for a non-null JSON object */
    boolean isObject();

    boolean isPrimitive();

    default boolean isString() {
        return jsonType() == JsonType.String;
    }

    JsonType jsonType();

    default void onProperties(BiConsumer<String, IJsonValue> key_value) {
        if (isObject()) {
            asObject().forEach(key_value);
        }
    }

    /**
     * Resolve a JSON path created by {@link JsonPaths}.
     *
     * @param jsonPath
     * @return
     */
    default @Nullable IJsonValue resolve(Object jsonPath) {
        List<Object> path = JsonPaths.of(jsonPath);

        // base case
        if (path.isEmpty()) return this;

        // take first step
        Object step = path.get(0);
        if (step instanceof String) {
            String propertyKey = (String) step;
            // try to resolve property
            if (isObject()) {
                IJsonValue value = asObject().get(propertyKey);
                if (value != null) {
                    return value.resolve(path.subList(1, path.size()));
                }
            } else {
                throw new IllegalArgumentException("Cannot resolve property on non-object value");
            }
        } else if (step instanceof Integer) {
            int index = (Integer) step;
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

    /**
     * See {@link JaJson}
     */
    default Object toJaJsonValue() {
        if (isPrimitive()) {
            return asPrimitive().toJaJsonPrimitive();
        } else if (isArray()) {
            return asArray().toJaJsonList();
        } else if (isObject()) {
            return asObject().toJaJsonMap();
        }
        throw new IllegalStateException("Unknown JsonType: " + jsonType());
    }

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        fire(w);
        return w.jsonString();
    }

    default XmlFragmentString toXmlFragmentString() {
        switch (jsonType()) {
            case XmlString:
                return this.toXmlFragmentString();
            case String:
                return XmlFragmentString.ofPlainText(asString());
            case Object:
                IJsonObject obj = asObject();
                if (obj.hasProperty(IJsonXmlString.XML)) {
                    IJsonValue xml = obj.get_(IJsonXmlString.XML);
                    if (xml.isString()) {
                        // we can map if no wrong properties
                        long count = obj.properties().count();
                        if (count == 1) {
                            return XmlFragmentString.of(xml.asString(), JSON.XmlSpace.auto.toXml_XmlSpace());
                        }
                        if (count == 2 && obj.hasProperty(IJsonXmlString.XML_SPACE)) {
                            IJsonValue xmlSpaceStr = obj.get_(IJsonXmlString.XML_SPACE);
                            if (xmlSpaceStr.isString()) {
                                JSON.XmlSpace xmlSpace = JSON.XmlSpace.parseJson(xmlSpaceStr.asString());
                                return XmlFragmentString.of(xml.asString(), xmlSpace.toXml_XmlSpace());
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("Could not parse JSON object as XmlString");
            default:
                throw new IllegalStateException("Unexpected value to convert to XmlFragmentString: " + jsonType() + " JSON=" + this.toJsonString());
        }
    }

}
