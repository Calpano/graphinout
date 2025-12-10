package com.graphinout.foundation.pure.vector;

import com.fasterxml.jackson.annotation.JsonValue;
import com.graphinout.foundation.pure.functional.TriConsumer;
import com.graphinout.foundation.pure.log.Logger;
import com.graphinout.foundation.pure.value.BooleanRef;
import com.graphinout.foundation.pure.value.DoubleRef;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.ObjDoubleConsumer;

import static com.graphinout.foundation.pure.log.LoggerFactory.getLogger;


/**
 * a generic Map from K to Double
 *
 * @param <K>
 */
public interface ISparseVec<K> {

    @FunctionalInterface
    interface IFoldConsumer<K> extends TriConsumer<K, Double, Double> {

        void accept(K key, double a, double b);

        @Override
        default void accept(K key, Double a, Double b) {
            accept(key, a.doubleValue(), b.doubleValue());
        }

    }

    Logger _log = getLogger(ISparseVec.class);

    static <K> double cosineDistance(ISparseVec<K> a, ISparseVec<K> b) {
        return 1 - cosineSimilarity(a, b);
    }

    static <K> double cosineSimilarity(ISparseVec<K> a, ISparseVec<K> b) {
        return dot(a, b) / (a.magnitude() * b.magnitude());
    }

    /**
     * Small performance improvement when using a pre-normalized vector.
     *
     * @param qNormalized is known to be normalized
     * @param doc         may or may not be normalized
     * @return cosine similarity
     */
    static <K> double cosineSimilarityQNormalized(ISparseVec<K> qNormalized, ISparseVec<K> doc) {
        return dot(qNormalized, doc) / (doc.magnitude());
    }

    static <K> SparseVecFlex<K> createMutableFlex() {
        return new SparseVecFlex<>();
    }

    static <K> double dot(ISparseVec<K> a, ISparseVec<K> b) {
        final DoubleRef result = DoubleRef.createZero();
        a.forEachKeyValue((k, aScore) -> {
            double bScore = b.get(k);
            if (bScore > 0) result.value += aScore * bScore;
        });
        return result.value;
    }

    /** non-fast version, use {@link ISparseVec#foldWith(ISparseVec, IFoldConsumer)} instead. */
    static <K> void foldSlow(ISparseVec<K> a, ISparseVec<K> b, IFoldConsumer<K> consumer) {
        // first, traverse A
        a.forEachKeyValue((key, aScore) -> {
            double bScore = b.get(key);
            consumer.accept(key, aScore, bScore);
        });
        // then, traverse B, skipping common keys
        b.forEachKeyValue((key, bScore) -> {
            if (a.containsKey(key)) return;
            double aScore = a.get(key);
            consumer.accept(key, aScore, bScore);
        });
    }

    static <K> void forEachKeyInBoth(ISparseVec<K> a, ISparseVec<K> b, IFoldConsumer<K> k_aScore_bScore) {
        a.forEachKeyValue((k, aScore) -> {
            double bScore = b.get(k);
            if (bScore > 0) {
                k_aScore_bScore.accept(k, aScore, bScore);
            }
        });
    }

    static <T> boolean isInstance(Object o, Class<T> componentType) {
        if (!(o instanceof ISparseVec)) return false;
        try {
            @SuppressWarnings("unchecked") ISparseVec<T> vec = (ISparseVec<T>) o;
            // all keys must be of the given type
            BooleanRef b = new BooleanRef(true);
            vec.forEachKey(k -> {
                if (!b.value) return;
                if (!k.getClass().equals(componentType)) {
                    b.value = false;
                }
            });
            return b.value;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * For both vectors: component-wise: calc the min and max value per K.
     */
    static <K> double jaccardSimilarity(ISparseVec<K> a, ISparseVec<K> b) {
        // min and max-sum
        DoubleRef min = DoubleRef.create(0);
        DoubleRef max = DoubleRef.create(0);
        a.foldWith(b, (k, aScore, bScore) -> {
            min.value += Math.min(aScore, bScore);
            max.value += Math.max(aScore, bScore);
        });
        return min.value / max.value;
    }

    /**
     * r := a * d
     *
     * @return r
     */
    static <K> IMutableSparseVec<K> mul(ISparseVec<K> a, double d, IMutableSparseVec<K> r) {
        a.forEachKeyValue((k, v) -> r.add(k, v * d));
        return r;
    }

    static <K> IMutableSparseVec<K> mulComponents(ISparseVec<K> a, ISparseVec<K> b, IMutableSparseVec<K> r) {
        a.foldWith(b, (k, aScore, bScore) -> r.add(k, aScore * bScore));
        return r;
    }

    static <K> ISparseVec<K> of(Map<K, Double> map) {
        IMutableSparseVec<K> vec = createMutableFlex();
        map.forEach(vec::add);
        return vec;
    }

    @JsonValue
    default Map<K, Double> asMap() {
        Map<K, Double> map = new HashMap<>();
        forEachKeyValue(map::put);
        return map;
    }

    /**
     * A vector A contains another vector B if: (1) all keys of B are contained in A and (2) the values of B are <= the
     * respective values in A.
     */
    default boolean contains(ISparseVec<K> b) {
        if (b.size() > this.size()) return false;
        // are there keys in b that are not in a?
        if (!containsAllKeysOf(b)) {
            return false;
        }
        BooleanRef bool = new BooleanRef(true);
        forEachKeyValue((aKey, aValue) -> {
            if (!bool.value) return;
            double bValue = b.get(aKey);
            if (bValue > aValue) bool.value = false;
        });

        return bool.value;
    }

    /**
     * @return true if this vector contains all keys of other, false otherwise
     */
    default boolean containsAllKeysOf(ISparseVec<K> other) {
        if (other.size() > this.size()) return false;
        BooleanRef b = new BooleanRef(true);
        other.forEachKey(k -> {
            if (!b.value) return;
            if (!containsKey(k)) b.value = false;
        });
        return b.value;
    }

    boolean containsKey(K key);

    default double cosineSimilarityWith(ISparseVec<K> vec) {
        return ISparseVec.cosineSimilarity(this, vec);
    }

    void foldWith(ISparseVec<K> other, IFoldConsumer<K> consumer);

    void forEachKey(Consumer<K> keyConsumer);

    void forEachKeyValue(ObjDoubleConsumer<K> key_double);

    void forEachValue(DoubleConsumer doubleConsumer);

    double get(K key);

    default boolean hasSameKeysAs(ISparseVec<K> other) {
        if (this.size() != other.size()) return false;
        BooleanRef b = new BooleanRef(true);
        other.forEachKey(k -> {
            if (!b.value) return;
            if (!containsKey(k)) b.value = false;
        });
        return b.value;
    }

    default double magnitude() {
        DoubleRef result = DoubleRef.createZero();
        forEachValue(v -> result.value += v * v);
        return Math.sqrt(result.value);
    }

    /**
     * Normalize THIS vector length to 1 and store in given vector.
     *
     * @param result vector to store the result in: this vector normalized to length 1
     * @return result for convenience
     */
    default ISparseVec<K> normalize(IMutableSparseVec<K> result) {
        double magnitude = magnitude();
        if (magnitude == 0) return this;
        return mul(this, 1.0 / magnitude, result);
    }

    int size();

    default double sum() {
        DoubleRef d = DoubleRef.createZero();
        forEachValue(v -> d.value += v);
        return d.value;
    }


}
