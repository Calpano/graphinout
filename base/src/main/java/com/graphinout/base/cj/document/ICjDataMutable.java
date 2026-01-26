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

    default void add(List<IJsonContainerNavigationStep> path, String javaString) {
        add(path, factory().createString(javaString));
    }

    default void add(List<IJsonContainerNavigationStep> path, XmlFragmentString xmlFragmentString) {
        add(path, factory().createXmlString(xmlFragmentString.rawXml(), xmlFragmentString.xmlSpace()));
    }

    default void add(String propertyKey, String javaString) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), javaString);
    }

    default void add(String propertyKey, boolean b) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), IJsonFactory.INSTANCE.createBoolean(b));
    }

    default void add(String propertyKey, Number number) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), IJsonFactory.INSTANCE.createNumber(number));
    }

    default void add(String propertyKey, IJsonValue jsonValue) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), jsonValue);
    }

    void remove(String propertyKey);

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

    void removeJsonValue();

}
