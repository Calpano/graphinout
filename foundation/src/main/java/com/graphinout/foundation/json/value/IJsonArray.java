package com.graphinout.foundation.json.value;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.path.IJsonArrayNavigationStep;
import com.graphinout.foundation.json.path.IJsonNavigationPath;
import com.graphinout.foundation.json.writer.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public interface IJsonArray extends IJsonContainer {

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

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        forEach((value, index) -> {
            IJsonNavigationPath path2 = prefix.withAppend(IJsonArrayNavigationStep.of(index));
            if (value.isPrimitive()) {
                // send out
                path_primitive.accept(path2, value.asPrimitive());
            } else {
                // RECURSE
                value.forEachLeaf(path2, path_primitive);
            }
        });
    }

    /**
     * If you are sure not to get null (e.g. you are iterating over the array members), then use {@link #get(int)} to
     * get a non-null value.
     *
     * @param index
     * @return
     */
    @Nullable
    IJsonValue get(int index);

    default @NonNull IJsonValue get_(int index) {
        return Objects.requireNonNull(get(index));
    }

    default boolean hasIndex(int index) {
        assert index >= 0;
        return index < size();
    }

    default boolean isArray() {return true;}

    default boolean isObject() {return false;}

    default JsonType jsonType() {
        return JsonType.Array;
    }

    default List<Object> toJaJsonList() {
        List<Object> list = new ArrayList<>(size());
        forEach(element -> list.add(element.toJaJsonValue()));
        return list;
    }

}
