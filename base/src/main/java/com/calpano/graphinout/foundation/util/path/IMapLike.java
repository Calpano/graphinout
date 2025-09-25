package com.calpano.graphinout.foundation.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An interface that provides a map-like view of an object, abstracting over the underlying data structure.
 * <p>
 * This allows the {@link PathResolver} to navigate objects that are not standard {@link java.util.Map}s but can be
 * treated as such.
 */
public interface IMapLike {

    IMapLike EMPTY = new IMapLike() {
        @Override
        public Object get(String key) {
            return null;
        }

        @Override
        public List<String> keys() {
            return List.of();
        }
    };

    /**
     * Creates an {@link IMapLike} instance from a list of keys and a function to retrieve values.
     *
     * @param keys the list of keys.
     * @param fun  a function that takes a key and returns the corresponding value.
     * @return a new {@link IMapLike} instance.
     */
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

    /**
     * Creates an {@link IMapLike} instance from a key supplier and a value retrieval function.
     * <p>
     * This is useful for cases where the keys are computed lazily.
     *
     * @param keySupplier a supplier for the list of keys.
     * @param fun         a function that takes a key and returns the corresponding value.
     * @return a new {@link IMapLike} instance.
     */
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

    /**
     * Creates a trivial wrapper for a standard {@link java.util.Map}.
     *
     * @param javaMap the map to wrap.
     * @return a new {@link IMapLike} instance that delegates to the given map.
     */
    static IMapLike ofMap(Map<String, ?> javaMap) {
        return of(new ArrayList<String>(javaMap.keySet()), javaMap::get);
    }

    /**
     * Creates an {@link IMapLike} representing a single key-value pair.
     *
     * @param key   the single key.
     * @param value the single value.
     * @return a new {@link IMapLike} instance.
     */
    static IMapLike ofProperty(String key, Object value) {
        return ofProperty(key, () -> value);
    }

    /**
     * Creates an {@link IMapLike} representing a single key-value pair with a lazily supplied value.
     *
     * @param key      the single key.
     * @param supplier a supplier for the single value.
     * @return a new {@link IMapLike} instance.
     */
    static IMapLike ofProperty(String key, Supplier<Object> supplier) {
        return of(List.of(key), k -> k.equals(key) ? supplier.get() : null);
    }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key.
     */
    Object get(String key);

    /**
     * Returns a list of all keys in this map-like structure.
     *
     * @return a {@link List} of keys.
     */
    List<String> keys();

}
