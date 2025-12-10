package com.graphinout.foundation.pure.vector;

import com.graphinout.foundation.pure.value.DoubleRef;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.ObjDoubleConsumer;


/**
 * Always normalized to 1.
 */
@SuppressWarnings({"unused", "PatternVariableCanBeUsed"})
public class SparseVecFast<K extends Comparable<K>> implements ISparseVec<K> {

    static class KV<K extends Comparable<K>> implements Comparable<KV<K>> {

        final K key;
        final double d;

        KV(K key, double d) {
            this.key = key;
            this.d = d;
        }

        @Override
        public int compareTo(SparseVecFast.@NonNull KV<K> o) {
            return this.key.compareTo(o.key);
        }

    }

    private final K[] keys;
    private final double[] values;

    public SparseVecFast(K[] keys, double[] values) {
        this.keys = keys;
        this.values = values;
    }

    public SparseVecFast(ISparseVec<K> vec) {
        ArrayList<KV<K>> list = new ArrayList<>(vec.size());
        DoubleRef magnitude = DoubleRef.createZero();
        vec.forEachKeyValue((k, v) -> //
        {
            //noinspection Convert2Diamond
            list.add(new KV<K>(k, v));
            // while we need to stream all k-vs anyway, compute magnitude
            magnitude.value += v * v;
        });
        magnitude.value = Math.sqrt(magnitude.value);

        Collections.sort(list);
        //noinspection unchecked
        this.keys = (K[]) new Comparable[list.size()];
        this.values = new double[list.size()];

        for (int i = 0; i < list.size(); i++) {
            KV<K> kv = list.get(i);
            this.keys[i] = kv.key;
            // as we have to stream a second time after sorting, this is the perfect time
            // to normalize the values to 1.
            this.values[i] = kv.d / magnitude.value;
        }
    }

    @Override
    public boolean containsKey(K key) {
        for (K k : keys) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void foldWith(ISparseVec<K> other, IFoldConsumer<K> consumer) {
        if (other instanceof SparseVecFast<?> ) {
            SparseVecFast<K> o = (SparseVecFast<K>) other;
            // do a merge-sort like traversal with two index vars
            int i = 0;
            int j = 0;
            while (i < keys.length && j < o.keys.length) {
                int cmp = keys[i].compareTo(o.keys[j]);
                if (cmp < 0) {
                    // a is before b, b is null for this key
                    consumer.accept(keys[i], values[i], 0.);
                    i++;
                } else if (cmp > 0) {
                    // b is before a, a is null for this key
                    consumer.accept(o.keys[j], 0., o.values[j]);
                    j++;
                } else {
                    // a and b both have the key set
                    consumer.accept(keys[i], values[i], o.values[j]);
                    i++;
                    j++;
                }
            }
        } else {
            ISparseVec.foldSlow(this, other, consumer);
        }
    }

    @Override
    public void forEachKey(Consumer<K> keyConsumer) {
        for (K k : keys) {
            keyConsumer.accept(k);
        }
    }

    @Override
    public void forEachKeyValue(ObjDoubleConsumer<K> key_double) {
        for (int i = 0; i < keys.length; i++) {
            key_double.accept(keys[i], values[i]);
        }
    }

    @Override
    public void forEachValue(DoubleConsumer doubleConsumer) {
        for (double d : values) {
            doubleConsumer.accept(d);
        }
    }

    @Override
    public double get(K key) {
        int i = indexOf(key);
        if (i < 0) {
            return 0.;
        } else {
            return values[i];
        }
    }

    @Override
    public double magnitude() {
        return 1.;
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public String toString() {
        TreeMap<K, Double> map = new TreeMap<>();
        forEachKeyValue(map::put);

        StringBuilder b = new StringBuilder();
        b.append("(\n");
        map.forEach((k, v) -> b.append("\"").append(k).append("\"").append(", ").append(v).append(" //\n"));
        b.append(")\n");
        return b.toString();
    }

    private int indexOf(K key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

}
