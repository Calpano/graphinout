package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import java.util.List;

public interface ICjDataMutable extends ICjData {

    /**
     * Add to the current state.
     *
     * @param path      where to add, may be empty. See {@link IJsonContainerNavigationStep#pathOf(Object...)}
     * @param jsonValue to set at the end of the path
     */
    void add(List<IJsonContainerNavigationStep> path, IJsonValue jsonValue);

    default void add(List<IJsonContainerNavigationStep> path, String javaString) {
        add(path, factory().createString(javaString));
    }

    default void add(List<IJsonContainerNavigationStep> path, XmlFragmentString xmlFragmentString) {
        add(path, factory().createXmlString(xmlFragmentString.rawXml(), xmlFragmentString.xmlSpace()));
    }

    default void addProperty(String propertyKey, String javaString) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), javaString);
    }

    default void addProperty(String propertyKey, IJsonValue jsonValue) {
        add(IJsonContainerNavigationStep.pathOf(propertyKey), jsonValue);
    }

    /**
     * @param jsonValue to set
     * @throws IllegalStateException if data was already set
     */
    default void setJsonValue(IJsonValue jsonValue) throws IllegalStateException {
        if (jsonValue() != null)
            throw new IllegalStateException("data already set");
        add(List.of(), jsonValue);
    }

}
