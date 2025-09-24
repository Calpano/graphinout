package com.calpano.graphinout.foundation.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IMapLike {

    static IMapLike of(List<String> keys, Function<String, Object> fun) {
        return new IMapLike() {
            @Override
            public Object get(String key) {
                return fun.apply(key);
            }

            @Override
            public List<String> keys() {
                return keys;
            }
        };
    }

    static IMapLike of(Supplier<List<String>> keySupplier, Function<String, Object> fun) {
        return new IMapLike() {
            @Override
            public Object get(String key) {
                return fun.apply(key);
            }

            @Override
            public List<String> keys() {
                return keySupplier.get();
            }
        };
    }

    /** The trivial wrapper */
    static IMapLike ofMap(Map<String, ?> javaMap) {
        return of(new ArrayList<String>(javaMap.keySet()), javaMap::get);
    }

    static IMapLike ofProperty(String key, Object value) {
        return ofProperty(key, () -> value);
    }

    static IMapLike ofProperty(String key, Supplier<Object> supplier) {
        return of(List.of(key), k -> k.equals(key) ? supplier.get() : null);
    }

    Object get(String key);

    List<String> keys();

}
