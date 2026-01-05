package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Mutable */
public interface IJsonObjectMutable extends IJsonObjectAppendable, IJsonValueMutable {

    default IJsonObjectMutable add(String key, String value) {
        addProperty(key, factory().createString(value));
        return this;
    }

    default IJsonObjectMutable add(String key, Number value) {
        addProperty(key, factory().createNumber(value));
        return this;
    }

    default IJsonObjectMutable add(String key, boolean b) {
        addProperty(key, factory().createBoolean(b));
        return this;
    }

    default IJsonObjectMutable add(String key, IJsonValue value) {
        addProperty(key, value);
        return this;
    }

    default IJsonObjectMutable addAllFromJaJson(@NonNull Map<String, Object> map) {
        map.forEach((k, v) -> {
            if (v == null) {
                add(k, factory().createNull());
            } else if (v instanceof String) {
                add(k, (String) v);
            } else if (v instanceof Number) {
                add(k, (Number) v);
            } else if (v instanceof Boolean) {
                add(k, (Boolean) v);
            } else if (v instanceof Map) {
                addObject(k, sub -> {
                    //noinspection unchecked
                    sub.addAllFromJaJson((Map<String, Object>) v);
                });
            } else if (v instanceof List) {
                addArray(k, sub -> {
                    //noinspection unchecked
                    sub.addAllFromJaJson((List<Object>) v);
                });
            } else {
                throw new IllegalArgumentException("Unknown type " + v.getClass().getName());
            }
        });
        return this;
    }

    default void addArray(String key, Consumer<IJsonArrayMutable> arrayMutable) {
        IJsonArrayMutable a = factory().createArrayMutable();
        arrayMutable.accept(a);
        addProperty(key, a);
    }

    /**
     * Add value if value is not null
     *
     * @param key
     * @param value
     * @return
     */
    default IJsonObjectMutable addMaybe(String key, @Nullable String value) {
        if (value != null) add(key, value);
        return this;
    }

    default IJsonObjectMutable addMaybe(String key, @Nullable IJsonValue value) {
        if (value != null) add(key, value);
        return this;
    }

    default IJsonObjectMutable addMaybe(String key, @Nullable Number value) {
        if (value != null) add(key, value);
        return this;
    }

    default IJsonObjectMutable addMaybe(String key, @Nullable Boolean value) {
        if (value != null) add(key, value);
        return this;
    }

    default IJsonObjectMutable addNull(String key) {
        addProperty(key, factory().createNull());
        return this;
    }

    default IJsonObjectMutable addObject(String key, Consumer<IJsonObjectMutable> objectMutable) {
        IJsonObjectMutable o = factory().createObjectMutable();
        objectMutable.accept(o);
        addProperty(key, o);
        return this;
    }

    /**
     * @return this, the object at which the property was removed
     */
    IJsonObjectMutable removeProperty(String key);

    default void removePropertyIf(Predicate<String> keyTest) {
        for (String key : Java9.Set.copyOf(keys())) {
            if (keyTest.test(key)) {
                removeProperty(key);
            }
        }
    }

    default void setArray(String key, IJsonArray array) {
        setProperty(key, array);
    }

    default void setArray(String key, Consumer<IJsonArrayMutable> array) {
        IJsonArrayMutable arrayMutable = factory().createArrayMutable();
        array.accept(arrayMutable);
        setProperty(key, arrayMutable);
    }

    default void setBoolean(String key, boolean b) {
        setProperty(key, factory().createBoolean(b));
    }

    default void setNull(String key) {
        setProperty(key, factory().createNull());
    }

    default void setNumber(String key, double d) {
        setProperty(key, factory().createNumber(d));
    }

    default void setObject(String key, Consumer<IJsonObjectMutable> object) {
        IJsonObjectMutable objectMutable = factory().createObjectMutable();
        object.accept(objectMutable);
        setProperty(key, objectMutable);
    }

    default void setObject(String key, IJsonObject object) {
        setProperty(key, object);
    }

    default void setProperty(String key, IJsonValue jsonValue) {
        removeProperty(key);
        addProperty(key, jsonValue);
    }

    default void setString(String key, String value) {
        setProperty(key, factory().createString(value));
    }

}
