package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.path.IJsonNavigationPath;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonArrayAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MagicMutableJsonValue implements IMagicMutableJsonValue {

    private final IJsonFactory factory;
    /** null = empty, which is different from a JSON null */
    private IJsonValue value = null;

    public MagicMutableJsonValue(IJsonFactory factory, @Nullable IJsonValue value) {
        this.factory = factory;
        this.value = value;
    }

    public void add(IJsonValue jsonValue) {
        addMerge(jsonValue);
    }

    public void addMerge(IJsonValue value) {
        if (isEmpty()) {
            this.value = value;
        } else {
            // depends on current type
            switch (this.jsonType().valueType()) {
                case Array -> {
                    IJsonArrayAppendable array = (IJsonArrayAppendable) this.value.asArray();
                    array.add(value);
                }
                case Primitive -> {
                    // magic cast to array
                    IJsonValue prevValue = this.value;
                    IJsonArrayAppendable array = factory.createArrayAppendable();
                    array.add(prevValue);
                    array.add(value);
                    this.value = array;
                }
                case Object ->
                        throw new IllegalStateException("Current value is not an array/primitive, cannot add value");
            }
        }
    }

    @Override
    public IJsonObjectAppendable addProperty(List<String> propertySteps, IJsonValue value) {
        appendMerge(propertySteps, value);
        return this;
    }

    @Override
    public void addProperty(String propertyKey, Consumer<IMagicMutableJsonValue> subValueConsumer) {
        MagicMutableJsonValue leaf = new MagicMutableJsonValue(factory, null);
        addProperty(propertyKey, leaf);
        subValueConsumer.accept(leaf);
    }

    @Override
    public void append(List<String> propertySteps, IJsonValue value) {
        appendMerge(propertySteps, value);
    }

    public IJsonPrimitive asPrimitive() throws ClassCastException {
        if (value == null) return null;
        return value.asPrimitive();
    }

    @Override
    public Object base() {
        return this;
    }

    @Override
    public IJsonFactory factory() {
        return factory;
    }

    @Override
    public void fire(JsonWriter jsonWriter) {
        if (value != null) {
            value.fire(jsonWriter);
        }
    }

    @Override
    public void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        if (value == null) return;
        value.forEachLeaf(prefix, path_primitive);
    }

    @Nullable
    public IJsonValue get(String key) {
        if (value == null) return null;
        if (value.jsonType().valueType() == JsonType.ValueType.Object) {
            return value.asObject().get(key);
        }
        return null;
    }

    @Nullable
    public IJsonValue get(int index) {
        if (value == null) throw new IllegalStateException();
        if (value.jsonType().valueType() == JsonType.ValueType.Array) {
            return value.asArray().get(index);
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isArray() {
        return value != null && value.isArray();
    }

    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public boolean isObject() {
        return value != null && value.isObject();
    }

    @Override
    public boolean isPrimitive() {
        return value != null && value.isPrimitive();
    }

    @Override
    public JsonType jsonType() {
        if (value == null) throw new IllegalStateException();
        return value.jsonType();
    }

    @Override
    public IJsonValue jsonValue() {
        return value;
    }


    public Set<String> keys() {
        if (value == null) return Collections.emptySet();
        if (value.jsonType().valueType() == JsonType.ValueType.Object) {
            return value.asObject().keys();
        }
        throw new IllegalStateException();
    }

    @Override
    public void set(IJsonValue value) throws IllegalStateException {
        if (this.isEmpty()) {
            this.value = value;
        } else {
            throw new IllegalStateException("Cannot set value on non-empty value");
        }
    }

    public int size() {
        if (value == null) return 0;
        return switch (value.jsonType().valueType()) {
            case Object -> value.asObject().size();
            case Array -> value.asArray().size();
            case Primitive -> 1;
        };
    }

    private void appendMerge(List<String> propertySteps, IJsonValue value) {
        if (propertySteps.isEmpty()) {
            addMerge(value);
        } else {
            // get/createObject first property, then use path with head removed to retry
            String firstStep = propertySteps.getFirst();
            List<String> subList = propertySteps.subList(1, propertySteps.size());
            MagicMutableJsonValue leaf = navigateTo(firstStep);
            // RECURSE
            leaf.appendMerge(subList, value);
        }
    }

    private MagicMutableJsonValue magicToObjectWithProperty(String propertyStep, Consumer<IJsonObjectAppendable> objectConsumer) {
        IJsonObjectAppendable appendableObject = factory.createObjectAppendable();
        this.value = appendableObject;
        MagicMutableJsonValue leaf = new MagicMutableJsonValue(factory, null);
        objectConsumer.accept(appendableObject);
        assert !appendableObject.hasProperty(propertyStep);
        appendableObject.addProperty(propertyStep, leaf);
        return leaf;
    }

    private MagicMutableJsonValue navigateTo(String propertyKey) {
        if (this.isEmpty()) {
            return magicToObjectWithProperty(propertyKey, o -> {});
        } else {
            // depends on current type
            switch (value.jsonType().valueType()) {
                case Primitive -> {
                    IJsonValue prevValue = this.value;
                    return magicToObjectWithProperty(propertyKey, o -> //
                            o.addProperty("value", prevValue));
                }
                case Object -> {
                    MagicMutableJsonValue leaf = new MagicMutableJsonValue(factory, null);
                    ((IJsonObjectAppendable) value).addProperty(propertyKey, leaf);
                    return leaf;
                }
                case Array -> throw new IllegalStateException("Array cannot get property '" + propertyKey + "'");
                default -> throw new AssertionError("Unexpected value: " + value.jsonType().valueType());
            }
        }
    }


}
