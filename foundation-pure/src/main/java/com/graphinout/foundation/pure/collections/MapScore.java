package com.graphinout.foundation.pure.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MapScore<K> {

    private final Map<K, Double> map = new LinkedHashMap<>();

    @JsonCreator
    public MapScore(Map<K, Double> map) {
        this.map.putAll(map);
    }

    public MapScore() {
    }

    public static <K> MapScore<K> create() {
        return new MapScore<>();
    }

    /** Jackson expects a 'getFoo()' */
    @JsonValue
    public Map<K, Double> asMap() {
        return map;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public Set<Map.Entry<K, Double>> entrySet() {
        return map.entrySet();
    }

    public void forEach(BiConsumer<K, Double> kv) {
        map.forEach(kv);
    }

    public void forEachCount(BiConsumer<K, Double> key_count) {
        map.forEach(key_count);
    }

    /**
     * @param highestCountsFirst determines sort direction
     * @return List
     */
    public List<Map.Entry<K, Double>> getSortedEntryMap(final boolean highestCountsFirst) {
        Comparator<Map.Entry<K, Double>> entryComparator = Map.Entry.comparingByValue();
        if (highestCountsFirst) {
            entryComparator = entryComparator.reversed();
        }
        return Java9.Stream.toList(map.entrySet().stream().sorted(entryComparator));
    }

    /**
     * Return the highest (or lowest) n entries. Sorted by (1) their count, (2) if comparable, sorted secondary by the
     * natural ordering of the keys.
     *
     * @param numberOfResults
     * @param highestCountsFirst if false, lowest entries are returned
     * @return @NeverNull
     */
    public List<K> getTop_k_SortedBy(final int numberOfResults, final boolean highestCountsFirst) {
        final List<Map.Entry<K, Double>> list = getSortedEntryMap(highestCountsFirst);

        final int resSize = Math.min(list.size(), numberOfResults);
        final List<K> result = new ArrayList<>(resSize);
        for (int i = 0; i < resSize; i++) {
            result.add(list.get(i).getKey());
        }
        return result;
    }

    /** increment */
    public void add(K key, double score) {
        map.compute(key, (k, v) -> v == null ? score : v + score);
    }

    /** add 1 more */
    public void add(K key) {
        add(key, 1);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Double lookup(K key) {
        return map.getOrDefault(key, 0.);
    }

    public @Nullable K max(Comparator<K> tieBreaker) {
        return map.entrySet().stream() //
                .max(Map.Entry.<K, Double>comparingByValue().reversed().thenComparing(Map.Entry::getKey, tieBreaker)) //
                .map(Map.Entry::getKey) //
                .orElse(null);
    }

    public void removeIf(Predicate<K> keyPredicate) {
        map.entrySet().removeIf(entry -> keyPredicate.test(entry.getKey()));
    }

    public double score(K key) {
        return Nullables.nonNull(lookup(key), 0.);
    }

    /** keys */
    public int size() {
        return map.size();
    }

    public Stream<Map.Entry<K, Double>> streamEntries() {
        return map.entrySet().stream();
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
