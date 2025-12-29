package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.functional.TriConsumer;
import com.graphinout.foundation.pure.functional.TriFunction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MapMap<K, L, M> {

    private final Map<K, Map<L, M>> k_l_m = new HashMap<>();

    public static <K, L, E> MapMap<K, L, E> create() {
        return new MapMap<>();
    }

    public void add(K k, L l) {
        Map<L, M> sub = lookup(k);
        if (sub != null) {
            sub.remove(l);
        }
    }

    /** overwrite */
    public void add(K k, L l, M m) {
        mapK(k).put(l, m);
    }

    /**
     * @return previous
     */
    public @Nullable Map<L, M> addAll(K key1, Map<L, M> l_m) {
        return k_l_m.put(key1, l_m);
    }

    public void compute(K k, L l, TriFunction<K, L, M, M> fun) {
        // IMPROVE can be optimized with less map calls
        M old = get(k, l);
        M newValue = fun.apply(k, l, old);
        if (newValue == null) {
            add(k, l);
        } else {
            add(k, l, newValue);
        }
    }

    public void dump() {
        k_l_m.forEach((k, map) -> {
            map.forEach((l, m) -> System.out.println("  [ " + k + "][ " + l + "] = " + m));
        });
    }

    public void forEach(TriConsumer<K, L, M> k_l_m) {
        this.k_l_m.forEach((k, map) -> map.forEach((l, m) -> k_l_m.accept(k, l, m)));
    }

    public void forEach(K k, BiConsumer<L, M> l_m) {
        Map<L, M> sub = lookup(k);
        if (sub != null) {
            sub.forEach(l_m);
        }
    }

    public void forEachPair(BiConsumer<K, Map<L, M>> k_lm) {
        this.k_l_m.forEach(k_lm);
    }

    public @Nullable M get(K k, L l) {
        return k_l_m.getOrDefault(k, new HashMap<>()).getOrDefault(l, null);
    }

    public List<M> getKey1_Star(K key1) {
        Map<L, M> subMap = k_l_m.get(key1);
        if (subMap == null) return Java9.List.of();
        return Java9.List.copyOf(subMap.values());
    }

    public boolean isEmpty() {
        return k_l_m.isEmpty() || k_l_m.values().stream().allMatch(Map::isEmpty);
    }

    public Set<K> keySet() {
        return k_l_m.keySet();
    }

    public Map<L, M> lookup(K k) {
        return k_l_m.get(k);
    }

    public M lookup(K k, L l) {
        Map<L, M> sub = k_l_m.get(k);
        if (sub == null) return null;
        return sub.get(l);
    }

    public @Nullable M put(K key1, L key2, M entry) {
        Map<L, M> subMap = k_l_m.computeIfAbsent(key1, k -> new HashMap<>());
        return subMap.put(key2, entry);
    }

    public MapMap<L, K, M> reorderedLMK() {
        MapMap<L, K, M> result = new MapMap<>();
        forEach((k, l, m) -> result.add(l, k, m));
        return result;
    }

    public Stream<Map.Entry<K, Map<L, M>>> streamEntries() {
        return k_l_m.entrySet().stream();
    }

    /**
     * @param k        key
     * @param consumer receives never null
     */
    public void update(K k, Consumer<Map<L, M>> consumer) {
        boolean created = false;
        Map<L, M> sub = lookup(k);
        if (sub == null) {
            sub = new HashMap<>();
            created = true;
        }
        consumer.accept(sub);
        if (sub.isEmpty()) {
            k_l_m.remove(k);
        } else if (created) {
            k_l_m.put(k, sub);
        }
    }

    private @NonNull Map<L, M> mapK(K k) {
        return k_l_m.computeIfAbsent(k, k1 -> new HashMap<>());
    }

}
