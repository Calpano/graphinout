package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.functional.Nullables;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MapCountIndex<K> {

    private final Map<K, Integer> map = new LinkedHashMap<>();

    // TODO @JsonCreator
    public MapCountIndex(Map<K, Integer> map) {
        this.map.putAll(map);
    }

    public MapCountIndex() {
    }

    public static <K> MapCountIndex<K> create() {
        return new MapCountIndex<>();
    }

    /** Jackson expects a 'getFoo()' */
    // TODO @JsonValue
    public Map<K, Integer> asMap() {
        return map;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int count(K key) {
        return Nullables.nonNull(lookup(key), 0);
    }

    public void deIndex(K key) {
        map.remove(key);
    }

    public Set<Map.Entry<K, Integer>> entrySet() {
        return map.entrySet();
    }

    public void forEach(BiConsumer<K, Integer> kv) {
        map.forEach(kv);
    }

    public void forEachCount(BiConsumer<K, Integer> key_count) {
        map.forEach(key_count);
    }

    /** increment */
    public void index(K key, int count) {
        map.compute(key, (k, v) -> v == null ? count : v + count);
    }

    /** index 1 more */
    public void index(K key) {
        index(key, 1);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Integer lookup(K key) {
        return map.getOrDefault(key, 0);
    }

    public @Nullable K max(Comparator<K> tieBreaker) {
        return map.entrySet().stream() //
                .max(Map.Entry.<K, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey, tieBreaker)) //
                .map(Map.Entry::getKey) //
                .orElse(null);
    }

    public void removeIf(Predicate<K> keyPredicate) {
        map.entrySet().removeIf(entry -> keyPredicate.test(entry.getKey()));
    }

    /** keys */
    public int size() {
        return map.size();
    }

    public Stream<Map.Entry<K, Integer>> streamEntries() {
        return map.entrySet().stream();
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
