package com.graphinout.foundation.util;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MapMap<K, L, E> {

    private final Map<K, Map<L, E>> map = Maps.newHashMap();

    public static <K, L, E> MapMap<K, L, E> create() {
        return new MapMap<>();
    }

    public @Nullable E get(K key1, L key2) {
        Map<L, E> subMap = map.get(key1);
        return subMap == null ? null : subMap.get(key2);
    }

    public List<E> getKey1_Star(K key1) {
        Map<L, E> subMap = map.get(key1);
        if (subMap == null) return List.of();
        return List.copyOf(subMap.values());
    }

    public @Nullable E put(K key1, L key2, E entry) {
        Map<L, E> subMap = map.computeIfAbsent(key1, k -> Maps.newHashMap());
        return subMap.put(key2, entry);
    }

}
