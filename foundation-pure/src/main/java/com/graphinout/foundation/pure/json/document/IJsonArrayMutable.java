package com.graphinout.foundation.pure.json.document;

import java.util.function.Consumer;
import java.util.function.Function;

/** Mutable */
public interface IJsonArrayMutable extends IJsonArrayAppendable, IJsonValueMutable {

    default IJsonArrayMutable add(String s) {
        add(factory().createString(s));
        return this;
    }

    default IJsonArrayMutable add(boolean b) {
        add(factory().createBoolean(b));
        return this;
    }

    default IJsonArrayMutable add(Number n) {
        add(factory().createNumber(n));
        return this;
    }

    default IJsonArrayMutable addAll(double[] values) {
        for (double v : values) {
            add(factory().createDouble(v));
        }
        return this;
    }

    default <T> IJsonArrayMutable addAll(Iterable<T> values, Function<T,IJsonValue> mapFun) {
        for (T v : values) {
            add(mapFun.apply(v));
        }
        return this;
    }

    default <T> IJsonArrayMutable addAllAsString(Iterable<T> values, Function<T,String> mapFun) {
        for (T v : values) {
            add(mapFun.apply(v));
        }
        return this;
    }

    default void addArray(Consumer<IJsonArrayMutable> arrayMutable) {
        IJsonArrayMutable a = factory().createArrayMutable();
        arrayMutable.accept(a);
        add(a);
    }

    default IJsonArrayMutable addNull() {
        add(factory().createNull());
        return this;
    }

    default void addObject(Consumer<IJsonObjectMutable> objectMutable) {
        IJsonObjectMutable o = factory().createObjectMutable();
        objectMutable.accept(o);
        add(o);
    }

    void remove(int index) throws ArrayIndexOutOfBoundsException;

    void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException;

}
