package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.List;

/**
 * <h2>Rules</h2>
 * object cannot append; array cannot get property; primitive can append -> becomes array; primitive can get property ->
 * becomes object with <code>{ "value" : prev-value }</code>;
 * <p>
 * .API [ propertyKeys ]* (append|addProperty(p)) value
 */
public interface IMagicMutableJsonValue extends IJsonValue, IAppendableJsonObject, IAppendableJsonArray {

    /**
     * Object add property. Convert the current-value at propertySteps into <code>{ "value" : current-value }</code>
     *
     * @param propertySteps a chain of JSON property keys, intermediate objects are created as needed. The last step is
     *                      the new property key.
     * @param value         to set at propertySteps(last)
     */
    void addProperty(List<String> propertySteps, IJsonValue value);


    default IMagicMutableJsonValue addProperty(List<String> propertySteps, String valueString) {
        IJsonValue string = factory().createString(valueString);
        addProperty(propertySteps, string);
        return this;
    }

    default IMagicMutableJsonValue addProperty(String propertyKey, String valueString) {
        IJsonValue string = factory().createString(valueString);
        addProperty(propertyKey, string);
        return this;
    }

    default IMagicMutableJsonValue addProperty(String propertyKey, IJsonValue value) {
        addProperty(List.of(propertyKey), value);
        return this;
    }

    /**
     * Array append. Convert the current-value at propertySteps into <code>[ current-value, new-value]</code>
     *
     * @param propertySteps a chain of JSON property keys, intermediate objects are created as needed
     * @param value         to append
     */
    void append(List<String> propertySteps, IJsonValue value);

    IJsonFactory factory();

    @Override
    default void fire(JsonWriter jsonWriter) {
        if (isEmpty()) return;
        jsonValue().fire(jsonWriter);
    }

    @Override
    default boolean isArray() {
        if (isEmpty()) return false;
        return jsonType().valueType() == JsonType.ValueType.Array;
    }

    boolean isEmpty();

    @Override
    default boolean isObject() {
        if (isEmpty()) return false;
        return jsonType().valueType() == JsonType.ValueType.Object;
    }

    @Override
    default boolean isPrimitive() {
        if (isEmpty()) return false;
        return jsonType().valueType() == JsonType.ValueType.Primitive;
    }

    @Override
    default JsonType jsonType() {
        if (isEmpty()) return JsonType.Undefined;
        return jsonValue().jsonType();
    }

    IJsonValue jsonValue();

}
