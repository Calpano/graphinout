package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public interface IJsonObject extends IJsonValue {

    @Override
    default void fire(JsonWriter jsonWriter) {
        jsonWriter.objectStart();
        keys().forEach(key -> {
            IJsonValue value = get_(key);
            value.fire(jsonWriter);
        });
        jsonWriter.objectEnd();
    }

    @Nullable
    IJsonValue get(String key);

    @Nonnull
    default IJsonValue get_(String key) {
        return Objects.requireNonNull(get(key));
    }

    default JsonType jsonType() {
        return JsonType.Object;
    }

    Set<String> keys();

    int size();

}
