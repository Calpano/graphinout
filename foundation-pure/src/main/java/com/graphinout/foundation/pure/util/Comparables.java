package com.graphinout.foundation.pure.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;

public class Comparables {

    interface IExtractKeyAndCompare<T, K> extends Comparator<T> {

        static <T, K> Comparator<T> of(Function<T, @Nullable K> keyExtractFun, Comparator<K> keyComparator) {
            return new IExtractKeyAndCompare<T, K>() {
                @Override
                public int compareKeys(K keyA, K keyB) {
                    return keyComparator.compare(keyA, keyB);
                }

                @Override
                public K extractKey(T t) {
                    return keyExtractFun.apply(t);
                }
            };
        }

        @Override
        default int compare(@Nullable T a, @Nullable T b) {
            K aKey = extractKey(a);
            K bKey = extractKey(b);
            return Comparables.compareNullable(aKey, bKey, this::compareKeys);
        }

        int compareKeys(K keyA, K keyB);

        K extractKey(T t);

    }

    public static class By<T> {

        private final List<Comparator<T>> comparators = new ArrayList<>();

        public By<T> byComparator(Comparator<T> compareFun) {
            comparators.add(compareFun);
            return this;
        }

        public <K> By<T> byKey(Function<T, @Nullable K> keyExtractFun, Comparator<K> keyComparator) {
            comparators.add(IExtractKeyAndCompare.of(keyExtractFun, keyComparator));
            return this;
        }

        public <K extends Comparable<K>> By<T> byKey(Function<T, @Nullable K> keyExtractFun) {
            comparators.add(IExtractKeyAndCompare.of(keyExtractFun, Comparables::compareNullable));
            return this;
        }

        public <K> By<T> byList(Function<T, @Nullable List<K>> extractListOfKeysFun, Comparator<K> keyComparator) {
            comparators.add(IExtractKeyAndCompare.of(extractListOfKeysFun, //
                    (@Nullable List<K> listA, @Nullable List<K> listB) -> compareNullableLists(listA, listB, keyComparator)));
            return this;
        }

        public <K extends Comparable<K>> By<T> byStream(Function<T, @Nullable Stream<K>> extractStreamOfKeysFun) {
            return byStream(extractStreamOfKeysFun, Comparable::compareTo);
        }

        /**
         * IMPROVE we could compare without materialising both streams
         */
        public <K> By<T> byStream(Function<T, @Nullable Stream<K>> extractStreamOfKeysFun, Comparator<K> keyComparator) {
            return byList(x -> //
                            mapOrNull(extractStreamOfKeysFun.apply(x), stream -> stream.collect(Collectors.toList())) //
                    , keyComparator);
        }

        public int compare(T a, T b) {
            // start comparing at the head
            for (Comparator<T> comparator : comparators) {
                int cmp = comparator.compare(a, b);
                if (cmp != 0) return cmp;
            }
            return 0;
        }

    }

    public static <T> int compareLists(@NonNull List<T> a, @NonNull List<T> b, Comparator<T> elementComparator) {
        int cmp = Integer.compare(a.size(), b.size());
        if (cmp != 0) return cmp;
        for (int i = 0; i < a.size(); i++) {
            cmp = elementComparator.compare(a.get(i), b.get(i));
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    /**
     * Sort order with nulls: nulls first
     *
     * @param a
     * @param b
     * @param comparator
     * @param <T>
     * @return
     */
    public static <T> int compareNullable(@Nullable T a, @Nullable T b, @NonNull Comparator<@NonNull T> comparator) {
        if (a == null) {
            if (b == null) return 0;
            // a=null, b=present => A first
            return -1;
        } else {
            // a=present, b=null => B first
            if (b == null) return 1;
            return comparator.compare(a, b);
        }
    }

    public static <T extends Comparable<T>> int compareNullable(@Nullable T a, @Nullable T b) {
        return compareNullable(a, b, Comparable::compareTo);
    }

    public static <T> int compareNullableLists(@Nullable List<T> a, @Nullable List<T> b, Comparator<T> elementComparator) {
        return compareNullable(a, b, (listA, listB) -> compareLists(listA, listB, elementComparator));
    }

    public static int compareNullableString(String a, String b) {
        return compareNullable(a, b, String::compareTo);
    }

    /**
     * A brute-force comparison by comparing the #toString results
     *
     * @param a
     * @param b
     * @return
     */
    public static int compareNullableToString(Object a, Object b) {
        return compareNullable(a, b, Comparator.comparing(Object::toString));
    }

    /**
     * Uses as {@code Comparables.<YourTypeHere>comparing().by ... }
     *
     * @param <T>
     * @return
     */
    public static <T> By<T> comparing() {
        return new By<>();
    }

    /**
     * Objects to be compared may be null. Extracted keys may not.
     *
     * @param keyExtractFun
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T, K extends Comparable<K>> int comparingKeys(Function<@NonNull T, K> keyExtractFun, @Nullable T a, @Nullable T b) {
        return Comparables.<T>comparing().byKey(keyExtractFun).compare(a, b);
    }

}
