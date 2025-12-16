package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.Map;
import java.util.Set;

public interface IOcifExtensibleEntity {

    /** Copy unknown properties into map */
    default void copyUnknown(IJsonObject o) {
        for (String k : o.keys()) {
            if (!definedKeys().contains(k)) {
                map().put(k, o.get(k));
            }
        }
    }

    /** These keys are handled/interpreted */
    Set<String> definedKeys();

    Map<String, IJsonValue> map();

    default void set(String key, boolean b) {
        map().put(key, IJsonFactory.INSTANCE.createBoolean(b));
    }

    default void set(String key, Number n) {
        map().put(key, IJsonFactory.INSTANCE.createNumber(n));
    }

    default void set(String key, IJsonValue value) {
        map().put(key, value);
    }

    default void set(String key, String s) {
        map().put(key, IJsonFactory.INSTANCE.createString(s));
    }

}
