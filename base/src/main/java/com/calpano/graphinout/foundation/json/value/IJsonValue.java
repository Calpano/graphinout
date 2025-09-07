package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

public interface IJsonValue {

    JsonType jsonType();

    /** the underlying implementation */
    Object base();

    IJsonFactory factory();

    void fire(JsonWriter jsonWriter);

}
