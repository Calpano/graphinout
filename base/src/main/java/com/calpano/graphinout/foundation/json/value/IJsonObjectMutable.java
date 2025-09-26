package com.calpano.graphinout.foundation.json.value;

import java.util.Set;
import java.util.function.Predicate;

/** Mutable */
public interface IJsonObjectMutable extends IJsonObjectAppendable, IJsonValueMutable {

    /**
     * @return this, the object at which the property was removed
     */
    IJsonObjectMutable removeProperty(String key);

    default void removePropertyIf(Predicate<String> keyTest) {
        for (String key : Set.copyOf(keys())) {
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
