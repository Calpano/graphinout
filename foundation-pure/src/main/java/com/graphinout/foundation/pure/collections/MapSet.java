package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapSet<K, E> {

    public enum Kind {Fast, InsertionOrder}

    private final Map<K, Set<E>> map;

    protected MapSet(Map<K, Set<E>> map) {
        this.map = map;
    }

    public static <K, E> MapSet<K, E> create() {
        return create(Kind.Fast);
    }

    /**
     * @param kind one of a {@link Kind}
     */
    public static <K, E> MapSet<K, E> create(Kind kind) {
        return new MapSet<>(createMap(kind));
    }

    protected static <K, E> Map<K, Set<E>> createMap(Kind kind) {
        switch (kind) {
            case Fast:
                return new HashMap<>();
            case InsertionOrder:
                return new LinkedHashMap<>();
            default:
                throw new IllegalArgumentException("Unknown kind: " + kind);
        }

    }

    public static <K extends Comparable<K>, E> MapSet<K, E> createSorted() {
        return new MapSet<>(new TreeMap<>());
    }

    /** Auto-sorted by key */
    public static <E> MapSet<String, E> createString() {
        return new MapSet<>(new TreeMap<>());
    }

    public void add(K key, E entry) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(entry);
    }

    public void addAll(Collection<K> collection, E source) {
        collection.forEach(k -> add(k, source));
    }

    public void addIfMissing(K key, E value) {
        map.computeIfAbsent(key, k -> {
            Set<E> set = new HashSet<>();
            set.add(value);
            return set;
        });
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int count(K key) {
        return lookup(key).size();
    }

    public void forEachCount(BiConsumer<K, Integer> key_count) {
        map.forEach((key, values) -> key_count.accept(key, values.size()));
    }

    public void forEachPair(BiConsumer<K, E> kv) {
        map.forEach((key, values) -> //
                values.forEach(v -> kv.accept(key, v)));
    }

    public void forEachSet(BiConsumer<K, Set<E>> key_valueSet) {
        map.forEach((k, set) -> key_valueSet.accept(k, new HashSet<>(set)));
    }

    public Set<E> get(K key) {
        return map.getOrDefault(key, Java9.Set.of());
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public @NonNull Set<E> lookup(K key) {
        return map.getOrDefault(key, Collections.emptySet());
    }

    public void lookupForEach(K key, Consumer<E> consumer) {
        lookup(key).forEach(consumer);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void renameKey(K keyOld, K keyNew) {
        Set<E> values = map.remove(keyOld);
        if (values != null) {
            map.put(keyNew, values);
        }
    }

    /** keys */
    public int size() {
        return map.size();
    }

    /** flat view of all values */
    public Stream<E> streamValues() {
        return map.values().stream().flatMap(Collection::stream);
    }

    /**
     * Transform (key, set of values) into (key, count of values)
     */
    public Map<K, Integer> toMapOfCounts() {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

}
