package com.calpano.graphinout.foundation.util.path;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public interface IListLike {

    static IListLike of(int size, IntFunction<Object> fun) {
        return new IListLike() {
            @Override
            public Object get(int index) {
                return fun.apply(index);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    static IListLike of(Supplier<Integer> sizeSupplier, IntFunction<Object> fun) {
        return new IListLike() {
            @Override
            public Object get(int index) {
                return fun.apply(index);
            }

            @Override
            public int size() {
                return sizeSupplier.get();
            }
        };
    }

    /** The trivial wrapper */
    static IListLike ofList(List<?> javaList) {
        return of(javaList.size(), javaList::get);
    }

    Object get(int index);

    int size();

}
