package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.bridge.Java9;

import java.util.function.Predicate;

/** Mutable */
public interface IJsonObjectMutable extends IJsonObjectAppendable, IJsonValueMutable {

    /**
     * @return this, the object at which the property was removed
     */
    IJsonObjectMutable removeProperty(String key);

    default void removePropertyIf(Predicate<String> keyTest) {
        for (String key : Java9.Set.copyOf(keys())) {
            if (keyTest.test(key)) {
                removeProperty(key);
            }
        }
    }

    default void setProperty(String key, IJsonValue jsonValue) {
        removeProperty(key);
        addProperty(key, jsonValue);
    }

}
