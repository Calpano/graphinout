package com.calpano.graphinout.foundation.xml;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public class AttMaps {

    /**
     * @param map a mutable map
     */
    public static Map<String, String> changeIfPresent(Map<String, String> map, @NonNull String key, @NonNull String searchValue, @NonNull String replaceValue) {
        return changeIfPresent(map, key, currentValue -> searchValue.equals(currentValue) ? replaceValue : currentValue);
    }

    public static Map<String, String> changeIfPresent(Map<String, String> map, @NonNull String key, Function<String, String> fun) {
        String currentValue = map.get(key);
        if (currentValue == null) return map;
        String newValue = fun.apply(currentValue);
        if (newValue == null) {
            map.remove(key);
        } else {
            map.put(key, newValue);
        }
        return map;
    }

    /**
     * @param map read-only is ok
     */
    public static boolean containsKey(@Nullable Map<String, String> map, String key) {
        return map != null && map.containsKey(key);
    }

    public static void getOrDefault( @Nullable Map<String, String> attributes, String attName, String defaultValue, BiConsumer<String, Supplier<String>> name_value) {
        if(attributes==null) {
            name_value.accept(attName, () -> defaultValue);
            return;
        }
        String value = attributes.getOrDefault(attName,defaultValue);
        name_value.accept(attName, () -> value);
    }

    /**
     * @param map a mutable map
     */
    public static Map<String, String> removeIf(Map<String, String> map, @NonNull String key, @NonNull String value) {
        if (value.equals(map.get(key))) {
            map.remove(key);
        }
        return map;
    }

}
