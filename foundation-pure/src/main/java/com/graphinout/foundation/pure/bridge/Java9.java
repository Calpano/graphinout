package com.graphinout.foundation.pure.bridge;

import com.graphinout.foundation.pure.functional.Optionals;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * backported selected pieces of Java 9
 */
@SuppressWarnings(value = {"unused", "SequencedCollectionMethodCanBeUsed"})
public class Java9 {

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SimplifyOptionalCallChains"})
    public static class Optional {

        public static class OptionalChain<T> {

            private final java.util.List<Supplier<java.util.Optional<T>>> suppliers = new ArrayList<>();

            public java.util.Optional<T> build() {
                return suppliers.stream().reduce(java.util.Optional.empty(), Optionals::or, Optionals::or);
            }

            public OptionalChain<T> or(Supplier<java.util.Optional<T>> supplier) {
                suppliers.add(supplier);
                return this;
            }

            public OptionalChain<T> orNullable(Supplier<@Nullable T> supplier) {
                suppliers.add(() -> java.util.Optional.ofNullable(supplier.get()));
                return this;
            }

        }

        private Optional() {
        }

        public static <T> OptionalChain<T> chain() {
            return new OptionalChain<>();
        }

        /**
         * If a value is present, performs the given action with the value, otherwise performs the given empty-based
         * action.
         *
         * @param optional    the optional to operate on
         * @param action      the action to be performed, if a value is present
         * @param emptyAction the empty-based action to be performed, if no value is present
         * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
         *                              present and the given empty-based action is {@code null}.
         * @since 9
         */
        public static <T> void ifPresentOrElse(java.util.Optional<T> optional, Consumer<? super T> action, Runnable emptyAction) {
            if (optional.isPresent()) {
                action.accept(optional.get());
            } else {
                emptyAction.run();
            }
        }

        public static <T> T orElseThrow(java.util.Optional<T> optional) {
            if (optional.isPresent()) return optional.get();
            throw new IllegalStateException("Optional has no value");
        }

        /**
         * If a value is present, returns a sequential {@link Stream} containing only that value, otherwise returns an
         * empty {@code Stream}.
         *
         * @param optional The optional to operate on.
         * @return the optional value as a {@code Stream}
         * @apiNote This method can be used to transform a {@code Stream} of optional elements to a {@code Stream} of
         * present value elements:
         * <pre>{@code
         *     Stream<Optional<T>> os = ..
         *     Stream<T> s = os.flatMap(Optional::stream)
         * }</pre>
         * @since 9
         */
        public static <T> java.util.stream.Stream<T> stream(java.util.Optional<T> optional) {
            return optional.map(java.util.stream.Stream::of).orElseGet(java.util.stream.Stream::empty);
        }

    }

    public static class List {

        private List() {
        }

        public static <E> java.util.List<E> copyOf(Collection<E> collection) {
            return new ArrayList<>(collection);
        }

        public static <E> E getLast(java.util.List<E> list) {
            if (list.isEmpty()) throw new IllegalArgumentException("List is empty");
            return list.get(list.size() - 1);
        }


        @SafeVarargs
        public static <E> java.util.List<E> of(E... elements) {
            if (elements == null || elements.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.asList(elements);
        }

    }

    public static class Set {

        private Set() {
        }

        public static <E> java.util.Set<E> copyOf(java.util.Set<E> set) {
            return new HashSet<>(set);
        }

        @SafeVarargs
        @SuppressWarnings({"varargs"})
        public static <E> java.util.Set<E> of(E... elements) {
            if (elements == null || elements.length == 0) {
                return Collections.emptySet();
            }
            return new HashSet<>(Arrays.asList(elements));
        }

    }

    public static class Map {

        private Map() {
        }

        private static <K, V> java.util.Map<K, V> createDefaultMap() {
            return new LinkedHashMap<>();
        }

        public static <K, V> java.util.Map<K, V> of(K key, V value) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(key, value);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            map.put(k8, v8);
            return map;
        }

        public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
            java.util.Map<K, V> map = createDefaultMap();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            map.put(k4, v4);
            map.put(k5, v5);
            map.put(k6, v6);
            map.put(k7, v7);
            map.put(k8, v8);
            map.put(k9, v9);
            return map;
        }

    }

    public static class Stream {

        public static <T> java.util.stream.Stream<T> ofNullable(@Nullable T nullable) {
            return nullable == null ? java.util.stream.Stream.empty() : java.util.stream.Stream.of(nullable);
        }

        public static <T> java.util.List<T> toList(java.util.stream.Stream<T> stream) {
            ArrayList<T> list = new ArrayList<>();
            stream.forEach(list::add);
            return list;
        }

    }

    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static class String {

        /** {@code text.codePoints().allMatch(..)} */
        public static boolean codePoints_allMatch(java.lang.String text, IntPredicate predicate) {
            for (int i = 0; i < text.length(); i++) {
                int codePoint = text.codePointAt(i);
                if (!predicate.test(codePoint)) {
                    return false;
                }
                i += Character.charCount(codePoint);
            }
            return true;
        }

        /**
         * The full format spec is lengthy and complex. JS provides a shallow version.
         *
         * @param s
         * @param args
         * @return
         */
        // FIXME this one does not belong here, must go into PlatformShared or similar
        public static java.lang.String format(java.lang.String s, Object... args) {
            return s + " args:" + Arrays.toString(args);
            // return java.lang.String.format(s, args);
        }

        public static boolean isBlank(java.lang.String s) {
            return codePoints_allMatch(s, Java9::isWhitespace);
        }

        public static boolean isEmpty(java.lang.String s) {
            //noinspection SizeReplaceableByIsEmpty
            return s.length() == 0;
        }

        public static boolean isEmpty(StringBuilder sb) {
            return sb.length() == 0;
        }

    }

    public static class Objects {

        public static <T> @NonNull T requireNonNullElse(@Nullable T nullableValue, java.lang.String errorMsg) {
            if (nullableValue == null) {
                throw new NullPointerException(errorMsg);
            }
            return nullableValue;
        }

    }

    private Java9() {
    }

    /**
     * This is a non-Unicode method. Use TextTool for good Unicode methods.
     */
    public static void forEachCharacterIn(java.lang.String s, Consumer<Character> charConsumer) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            charConsumer.accept(c);
        }
    }

    /**
     * It is a Unicode space character (SPACE_SEPARATOR, LINE_SEPARATOR, or PARAGRAPH_SEPARATOR) but is not also a
     * non-breaking space ('\u00A0', '\u2007', '\u202F'). It is '\t', U+0009 HORIZONTAL TABULATION. It is '\n', U+000A
     * LINE FEED. It is '\u000B', U+000B VERTICAL TABULATION. It is '\f', U+000C FORM FEED. It is '\r', U+000D CARRIAGE
     * RETURN. It is '\u001C', U+001C FILE SEPARATOR. It is '\u001D', U+001D GROUP SEPARATOR. It is '\u001E', U+001E
     * RECORD SEPARATOR. It is '\u001F', U+001F UNIT SEPARATOR.
     *
     * @param cp
     * @return
     */
    public static boolean isWhitespace(int cp) {
        return Character.isWhitespace(cp) || (cp >= 0x0009 && cp <= 0x000D) || (cp >= 0x001C && cp <= 0x001F);
    }

    /**
     * Returns a predicate that is the negation of the supplied predicate. This is accomplished by returning result of
     * the calling {@code target.negate()}.
     *
     * @param <T>    the type of arguments to the specified predicate
     * @param target predicate to negate
     * @return a predicate that negates the results of the supplied predicate
     * @throws NullPointerException if target is null
     * @since 11
     */
    static <T> Predicate<T> not(Predicate<? super T> target) {
        java.util.Objects.requireNonNull(target);
        return t -> !target.test(t);
    }

}



