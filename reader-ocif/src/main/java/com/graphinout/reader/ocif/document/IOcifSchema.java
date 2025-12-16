package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.impl.OcifSchema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IOcifSchema {

    @Nullable String location();

    @Nullable String name();

    @Nullable IJsonObject schema();

    @NonNull String uri();

    static IOcifSchema of(String uri) {
        OcifSchema schema = new OcifSchema();
        schema.setUri(uri);
        return schema;
    }

}
