package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Mutable extension of {@link ICjData} that supports incrementally building the JSON value tree for CJ data.
 */
public interface ICjDataMutable extends ICjData {

    /**
     * Add to the current state.
     *
     * @param path      where to add, may be empty. See {@link IJsonContainerNavigationStep#pathOf(Object...)}
     * @param jsonValue to set at the end of the path
     */
    void add(List<IJsonContainerNavigationStep> path, @NonNull IJsonValue jsonValue);

    /**
     * Adds a string value at the specified navigation path.
     *
     * @param path       the path where the value should be added.
     * @param javaString the string to add.
     */
    default void add(List<IJsonContainerNavigationStep> path, String javaString) {
        add(path, factory().createString(javaString));
    }

    /**
     * Adds an XML fragment string value at the specified navigation path.
     *
     * @param path              the path where the value should be added.
     * @param xmlFragmentString the XML fragment to add.
     */
    default void add(List<IJsonContainerNavigationStep> path, XmlFragmentString xmlFragmentString) {
        add(path, factory().createXmlString(xmlFragmentString.rawXml(), xmlFragmentString.xmlSpace()));
    }

    /**
     * Adds a string property to the root of this data element.
     *
     * @param propertyKey the key of the property to add.
     * @param javaString  the string value to add.
     */
    default void add(String propertyKey, String javaString) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), javaString);
    }

    /**
     * Adds a boolean property to the root of this data element.
     *
     * @param propertyKey the key of the property to add.
     * @param b           the boolean value to add.
     */
    default void add(String propertyKey, boolean b) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), IJsonFactory.INSTANCE.createBoolean(b));
    }

    /**
     * Adds a numeric property to the root of this data element.
     *
     * @param propertyKey the key of the property to add.
     * @param number      the numeric value to add.
     */
    default void add(String propertyKey, Number number) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), IJsonFactory.INSTANCE.createNumber(number));
    }

    /**
     * Adds a JSON value property to the root of this data element.
     *
     * @param propertyKey the key of the property to add.
     * @param jsonValue   the JSON value to add.
     */
    default void add(String propertyKey, IJsonValue jsonValue) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), jsonValue);
    }

    /**
     * Removes a property from the root of this data element by its key.
     *
     * @param propertyKey the key of the property to remove.
     */
    void remove(String propertyKey);

    /**
     * Clears or removes the entire JSON value associated with this data element.
     */
    void removeJsonValue();

    /**
     * Sets (replacing if exists) a JSON value property at the root of this data element.
     *
     * @param propertyKey the key of the property to set.
     * @param jsonValue   the JSON value to set.
     */
    default void set(String propertyKey, IJsonValue jsonValue) {
        remove(propertyKey);
        add(propertyKey, jsonValue);
    }

    /**
     * @param jsonValue to set
     * @throws IllegalStateException if data was already set
     */
    default void setJsonValue(@NonNull IJsonValue jsonValue) throws IllegalStateException {
        if (jsonValue() != null)
            throw new IllegalStateException("data already set");
        add(List.of(), jsonValue);
    }

    /**
     * Sets (replacing if exists) a string property at the root of this data element.
     *
     * @param key   the key of the property to set.
     * @param value the string value to set.
     */
    default void setString(String key, String value) {
        set(key, factory().createString(value));
    }

}
