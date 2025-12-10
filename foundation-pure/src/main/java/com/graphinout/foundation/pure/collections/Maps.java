package com.graphinout.foundation.pure.collections;

import java.util.Map;
import java.util.function.Predicate;

public class Maps {

    public static <K, V> void removeIf(Map<K, V> map, K key, Predicate<V> test) {
        if (map.containsKey(key) && test.test(map.get(key))) {
            map.remove(key);
        }
    }

}
