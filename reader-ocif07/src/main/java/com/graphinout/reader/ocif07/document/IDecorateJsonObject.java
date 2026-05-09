package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.Map;
import java.util.Set;

public interface IDecorateJsonObject {

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

    /**
     * An immutable JSON map. Exposed the OCIF extensible entity as a JSON object map.
     */
    Map<String, IJsonValue> map();

}
