package com.calpano.graphinout.foundation.json.value;

/** Mutable */
public interface IJsonObjectMutable extends IJsonObjectAppendable, IJsonValueMutable {

    /**
     * @return this, the object at which the property was removed
     */
    IJsonObjectMutable removeProperty(String key);

    default void setProperty(String key, IJsonValue jsonValue) {
        removeProperty(key);
        addProperty(key, jsonValue);
    }

}
