package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.Ocifs.factory;

public interface IDecorateJsonObjectMutable extends IDecorateJsonObject {

    /**
     * A mutable JSON map. Exposed the OCIF extensible entity as a JSON object map.
     */
    Map<String, IJsonValue> map();

    default void set(String key, boolean b) {
        map().put(key, factory().createBoolean(b));
    }

    default void set(String key, Number n) {
        map().put(key, factory().createNumber(n));
    }

    default void set(String key, IJsonValue value) {
        map().put(key, value);
    }

    default void set(String key, String s) {
        map().put(key, factory().createString(s));
    }

}
