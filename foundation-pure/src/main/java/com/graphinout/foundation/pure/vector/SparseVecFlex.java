package com.graphinout.foundation.pure.vector;

import com.graphinout.foundation.pure.value.BooleanRef;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.ObjDoubleConsumer;

@SuppressWarnings("unused")
public class SparseVecFlex<K> implements IMutableSparseVec<K> {

    final Map<K, Double> values = new HashMap<>();

    public static <K> IMutableSparseVec<K> mul(ISparseVec<K> a, double d) {
        return ISparseVec.mul(a, d, ISparseVec.createMutableFlex());
    }

    @Override
    public boolean containsKey(K key) {
        return values.containsKey(key);
    }

    @Override
    public IMutableSparseVec<K> deIndex(@NonNull K k) {
        values.remove(k);
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISparseVec<?>)) return false;
        try {
            @SuppressWarnings("unchecked") ISparseVec<K> that = (ISparseVec<K>) o;
            if (this.size() != that.size()) return false;
            BooleanRef b = new BooleanRef(true);
            forEachKeyValue((k, v) -> {
                if (!b.value) return;
                if (that.get(k) != v) b.value = false;
            });
            return b.value;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public void foldWith(ISparseVec<K> other, IFoldConsumer<K> consumer) {
        ISparseVec.foldSlow(this, other, consumer);
    }

    @Override
    public void forEachKey(Consumer<K> keyConsumer) {
        values.keySet().forEach(keyConsumer);
    }

    @Override
    public void forEachKeyValue(ObjDoubleConsumer<K> key_double) {
        values.forEach(key_double::accept);
    }

    @Override
    public void forEachValue(DoubleConsumer doubleConsumer) {
        values.values().forEach(doubleConsumer::accept);
    }

    @Override
    public double get(K key) {
        return values.getOrDefault(key, 0.0);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public IMutableSparseVec<K> add(@NonNull K k, Double score) {
        assert  k != null;
        values.merge(k, score, Double::sum);
        return this;
    }

    public Consumer<K> indexWith(double score) {
        return k -> add(k, score);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("(\n");
        values.keySet().stream().sorted().forEach(k->{
            b.append("\"").append(k).append("\"").append(", ").append(values.get(k)).append(" //\n");
        });
        b.append(")\n");
        return b.toString();
    }

}
