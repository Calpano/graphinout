package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface IJsonArray extends IJsonValue {

    default void fire(JsonWriter jsonWriter) {
        jsonWriter.arrayStart();
        for (int i = 0; i < size(); i++) {
            get_(i).fire(jsonWriter);
        }
        jsonWriter.arrayEnd();
    }

    @Nullable
    IJsonValue get(int index);

    default @Nonnull IJsonValue get_(int index) {
        return Objects.requireNonNull(get(index));
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default JsonType jsonType() {
        return JsonType.Array;
    }

    int size();

}
