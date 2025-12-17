package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.bridge.Java9;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** Mutable */
public interface IJsonObjectMutable extends IJsonObjectAppendable, IJsonValueMutable {

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
