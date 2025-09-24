package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.value.IJsonArrayAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.List;
import java.util.function.Consumer;

/**
 * <h2>Rules</h2>
 * object cannot append; array cannot get property; primitive can append -> becomes array; primitive can get property ->
 * becomes object with <code>{ "value" : prev-value }</code>;
 * <p>
 * .API [ propertyKeys ]* (append|addProperty(p)) value
 */
public interface IMagicMutableJsonValue extends IJsonValue, IJsonObjectAppendable, IJsonArrayAppendable {

    /**
     * Object add property. Convert the current-value at propertySteps into <code>{ "value" : current-value }</code>
     *
     * @param propertySteps a chain of JSON property keys, intermediate objects are created as needed. The last step is
     *                      the new property key.
     * @param value         to set at propertySteps(last)
     */
    void addProperty(List<String> propertySteps, IJsonValue value);


    /**
     * @return this, the object at which the property was added
     */
    default IMagicMutableJsonValue addProperty(List<String> propertySteps, String valueString) {
        IJsonValue string = factory().createString(valueString);
        addProperty(propertySteps, string);
        return this;
    }

    /**
     * @return this, the object at which the property was added
     */
    default IMagicMutableJsonValue addProperty(String propertyKey, String valueString) {
        IJsonValue string = factory().createString(valueString);
        addProperty(propertyKey, string);
        return this;
    }

    /**
     * @return this, the object at which the property was added
     */
    default IMagicMutableJsonValue addProperty(String propertyKey, IJsonValue value) {
        addProperty(List.of(propertyKey), value);
        return this;
    }

    void addProperty(String propertyKey, Consumer<IMagicMutableJsonValue> subValue);

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

    /**
     * @param value to set
     * @throws IllegalStateException if the current state is not empty
     */
    void set(IJsonValue value) throws IllegalStateException;

    /** Set the value to the given String as a JSON primitive String value */
    default void set(String valueString) throws IllegalStateException {
        IJsonValue string = factory().createString(valueString);
        set(string);
    }

    /** Set the value to the given boolean as a JSON primitive boolean value */
    default void set(boolean value) throws IllegalStateException {
        IJsonValue string = factory().createBoolean(value);
        set(string);
    }

    /** Set the value to the given Number as a JSON primitive Number value */
    default void set(Number value) throws IllegalStateException {
        IJsonValue number = factory().createNumber(value);
        set(number);
    }

}
