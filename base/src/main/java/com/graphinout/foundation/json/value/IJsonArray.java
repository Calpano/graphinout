package com.graphinout.foundation.json.value;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.path.IJsonArrayNavigationStep;
import com.graphinout.foundation.json.path.IJsonNavigationPath;
import com.graphinout.foundation.json.writer.JsonWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        forEach((value,index) ->
        {
            IJsonNavigationPath path2 = prefix.withAppend(IJsonArrayNavigationStep.of(index));
            if(value.isPrimitive()) {
                // send out
                path_primitive.accept(path2, value.asPrimitive());
            } else {
                // RECURSE
                value.forEachLeaf(path2, path_primitive);
            }
        });
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

    default boolean isObject() {return false;}


    default JsonType jsonType() {
        return JsonType.Array;
    }

    default boolean hasIndex(int index) {
        assert index >= 0;
        return index < size();
    }

}
