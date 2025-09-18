package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public interface IJsonArray extends IJsonValue {

    default void fire(JsonWriter jsonWriter) {
        jsonWriter.arrayStart();
        for (int i = 0; i < size(); i++) {
            get_(i).fire(jsonWriter);
        }
        jsonWriter.arrayEnd();
    }

    default void forEach(ObjIntConsumer<IJsonValue> member_index) {
        for (int i = 0; i < size(); i++) {
            member_index.accept(get_(i), i);
        }
    }

    default void forEach(Consumer<IJsonValue> member) {
        for (int i = 0; i < size(); i++) {
            member.accept(get_(i));
        }
    }

    @Nullable
    IJsonValue get(int index);

    default @Nonnull IJsonValue get_(int index) {
        return Objects.requireNonNull(get(index));
    }

    default boolean isArray() {return true;}

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isObject() {return false;}

    default boolean isPrimitive() {return false;}

    default JsonType jsonType() {
        return JsonType.Array;
    }

    int size();

}
