package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.xml.XmlFragmentString;

import java.util.function.Consumer;

/** Mutable */
public interface IJsonObjectAppendable extends IJsonObject {

    /**
     * @return this, the object at which the property was added
     */
    default IJsonObjectAppendable addProperty(String key, String value) {
        return addProperty(key, factory().createString(value));
    }

    default IJsonObjectAppendable addProperty(String key, XmlFragmentString xmlFragmentString) {
        return addProperty(key, factory().createXmlString(xmlFragmentString.rawXml(), xmlFragmentString.xmlSpace()));
    }

    /**
     * @return this, the object at which the property was added
     * @throws IllegalStateException if property already present
     */
    IJsonObjectAppendable addProperty(String key, IJsonValue jsonValue) throws IllegalStateException;

    /**
     * @return this, the object at which the property was added
     */
    default IJsonObjectAppendable object(String key, Consumer<IJsonObjectAppendable> nestedObject) {
        IJsonObjectAppendable nested = factory().createObjectAppendable();
        // let consumer populate the nested object
        nestedObject.accept(nested);
        // add it
        return addProperty(key, nested);
    }

}
