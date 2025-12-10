package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.functional.TriConsumer;
import com.graphinout.foundation.pure.vector.IMutableSparseVec;
import com.graphinout.foundation.pure.vector.ISparseVec;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MapMapScore<K, L> {

    private final Map<K, Map<L, Double>> k_l_score = new HashMap<>();

    public static <K, L> MapMapScore<K, L> create() {
        return new MapMapScore<>();
    }

    public void remove(K k, L l) {
        Map<L, Double> sub = lookup(k);
        if (sub != null) {
            sub.remove(l);
        }
    }

    public void dump() {
        k_l_score.forEach((k, map) -> {
            map.forEach((l, score) -> System.out.println("  [ " + k + "][ " + l + "] = " + score));
        });
    }

    public void forEach(TriConsumer<K, L, Double> k_l_score) {
        this.k_l_score.forEach((k, map) -> map.forEach((l, score) -> k_l_score.accept(k, l, score)));
    }

    public void forEachWithScore(double score, BiConsumer<K, L> k_l) {
        forEach((k, l, s) -> {
            if (s == score) {
                k_l.accept(k, l);
            }
        });
    }

    public double get(K k, L l) {
        return k_l_score.getOrDefault(k, new HashMap<>()).getOrDefault(l, 0.0);
    }

    /** add score to existing value */
    public void add(K k, L l, double score) {
        mapK(k).merge(l, score, Double::sum);
    }

    public Map<L, Double> lookup(K k) {
        return k_l_score.get(k);
    }

    public double lookup(K k, L l) {
        Map<L, Double> sub = k_l_score.get(k);
        if (sub == null) return 0.0;
        return sub.getOrDefault(l, 0.0);
    }

    public Stream<Double> scores() {
        return k_l_score.values().stream().flatMap(map -> map.values().stream());
    }

    public ISparseVec<K> toSparseVector(L l) {
        IMutableSparseVec<K> s = ISparseVec.createMutableFlex();
        k_l_score.forEach((k, map) -> {
            // 1 lookup per k
            Double d = map.get(l);
            if (d != null)
                // 1 add per k
                s.add(k, d);
        });
        return s;
    }

    public Map<L, ? extends ISparseVec<K>> toSparseVectors() {
        Map<L, IMutableSparseVec<K>> result = new HashMap<>();
        k_l_score.forEach((k, map) -> {
            map.forEach((l, score) -> {
                // 1 lookup per k x l
                result.computeIfAbsent(l, l1 -> ISparseVec.createMutableFlex()) //
                        // 1 index per k x l
                        .add(k, score);
            });
        });
        return result;
    }

    /**
     * @param k        key
     * @param consumer receives never null
     */
    public void update(K k, Consumer<Map<L, Double>> consumer) {
        boolean created = false;
        Map<L, Double> sub = lookup(k);
        if (sub == null) {
            sub = new HashMap<>();
            created = true;
        }
        consumer.accept(sub);
        if (sub.isEmpty()) {
            k_l_score.remove(k);
        } else if (created) {
            k_l_score.put(k, sub);
        }
    }

    private @NonNull Map<L, Double> mapK(K k) {
        return k_l_score.computeIfAbsent(k, k1 -> new HashMap<>());
    }


}
