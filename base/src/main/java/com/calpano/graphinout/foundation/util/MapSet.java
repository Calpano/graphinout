package com.calpano.graphinout.foundation.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class MapSet<K, E> {

    private final Map<K, Set<E>> map = new HashMap<>();

    public static <K, E> MapSet<K, E> create() {
        return new MapSet<>();
    }

    public void add(K key, E entry) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(entry);
    }

    public void forEach(BiConsumer<K,Set<E>> key_set) {
        map.forEach(key_set);
    }

    public Set<E> get(K key) {
        return map.getOrDefault(key, Set.of());
    }

}
