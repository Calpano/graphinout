package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMap<K, L, E> {

    private final Map<K, Map<L, E>> map = new HashMap<>();

    public static <K, L, E> MapMap<K, L, E> create() {
        return new MapMap<>();
    }

    public @Nullable E get(K key1, L key2) {
        Map<L, E> subMap = map.get(key1);
        return subMap == null ? null : subMap.get(key2);
    }

    public List<E> getKey1_Star(K key1) {
        Map<L, E> subMap = map.get(key1);
        if (subMap == null) return Java9.List.of();
        return Java9.List.copyOf(subMap.values());
    }

    public @Nullable E put(K key1, L key2, E entry) {
        Map<L, E> subMap = map.computeIfAbsent(key1, k -> new HashMap<>());
        return subMap.put(key2, entry);
    }

}
