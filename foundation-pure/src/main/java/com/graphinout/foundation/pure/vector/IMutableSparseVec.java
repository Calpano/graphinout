package com.graphinout.foundation.pure.vector;

import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public interface IMutableSparseVec<K> extends ISparseVec<K> {

    IMutableSparseVec<K> deIndex(@NonNull K k);

    /**
     * Sums up the scores.
     * @return this for chaining
     */
    IMutableSparseVec<K> add(@NonNull K k, Double score);

    /**
     * Index each key as 1.
     */
    @SuppressWarnings({"unchecked"})
    default IMutableSparseVec<K> indexAll(K... keys) {
        if (keys == null) return this;
        for (K k : keys)
            add(k, 1.);
        return this;
    }

    default IMutableSparseVec<K> indexAll(ISparseVec<K> vec, Double factor) {
        vec.forEachKeyValue((k, v) -> add(k, v * factor));
        return this;
    }

    default void removeIf(BiFunction<K, Double, Boolean> key_value) {
        Set<K> toRemove = new HashSet<>();
        forEachKeyValue((k, v) -> {
            if (key_value.apply(k, v)) {
                toRemove.add(k);
            }
        });
        toRemove.forEach(this::deIndex);
    }

}
