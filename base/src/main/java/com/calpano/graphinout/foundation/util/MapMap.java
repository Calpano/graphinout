package com.calpano.graphinout.foundation.util;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

public class MapMap<K, L, E> {

    private final Map<K, Map<L, E>> map = Maps.newHashMap();

    public @Nullable E get(K key1, L key2) {
        Map<L, E> subMap = map.get(key1);
        return subMap == null ? null : subMap.get(key2);
    }

    public @Nullable E put(K key1, L key2, E entry) {
        Map<L, E> subMap = map.computeIfAbsent(key1, k -> Maps.newHashMap());
        return subMap.put(key2, entry);
    }

}
