package com.graphinout.foundation.pure.assertions;

import com.graphinout.foundation.pure.annotations.GwtIncompatible;
import com.graphinout.foundation.pure.text.Emoji;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Assertions {

    /** JVM version */
    private static class Vary extends Vary_j2cl {

        public static final boolean ASSERT_EVEN_ON_WHEN_JAVA_ASSERT_IS_OFF = true;

        @Override
        @GwtIncompatible("assert")
        public void assert_(boolean condition, Supplier<String> message) {
            if (ASSERT_EVEN_ON_WHEN_JAVA_ASSERT_IS_OFF) {
                if (!condition)
                    throw new AssertionError("Assert failed. " + message.get());
            } else {
                assert condition : message.get();
            }
        }

        @Override
        @GwtIncompatible("assert")
        public <T extends Throwable> void assert_throw(boolean condition, Supplier<T> supplier) throws T {
            // re-use Javas assert keyword to disable these checks if assertions are off.
            try {
                assert condition;
            } catch (AssertionError e) {
                throw supplier.get();
            }
        }

    }

    /* JS version */
    private static class Vary_j2cl {

        public void assert_(boolean condition, Supplier<String> message) {
            if (!condition) {
                throw new AssertionError("Assert failed. " + message.get());
            }
        }

        public <T extends Throwable> void assert_throw(boolean condition, Supplier<T> supplier) throws T {
            if (!condition) {
                throw supplier.get();
            }
        }

    }

    public static final boolean ASSERTIONS_COMPILED_IN = true;
    /**
     * ca be disabled/enabled at runtime
     */
    public static final boolean assertionsRuntimeEnabled = true;
    private static final Vary VARY = new Vary();

    public static void assertNonNull(Object o) {
        assertNonNull_(o);
    }

    public static void assertNonNull(Object... o) {
        assertNonNull_(o);
        assert_(o.length > 0, () -> "Empty check");
        for (int i = 0; i < o.length; i++) {
            int finalI = i;
            assertNonNull(o[i], () -> "Object at index " + finalI + " is null");
        }
    }

    public static void assertNonNull(Object o, String msg) {
        assert_(o != null, () -> "Expected non-null: " + msg);
    }

    public static void assertNonNull(Object o, Supplier<String> msgSupplier) {
        assert_(o != null, msgSupplier);
    }

    public static void assertNonNull_(Object o) {
        assert_(o != null, () -> "Expected to be non-null, but was null.");
    }

    public static <T> T assertReturnNonNull(Supplier<@Nullable T> supplier) {
        T t = supplier.get();
        assertNonNull_(t);
        return t;
    }

    public static void assert_(boolean condition) {
        assert_(condition, () -> "");
    }

    public static void assert_(BooleanSupplier conditionSupplier) {
        assert_(conditionSupplier, () -> "");
    }

    /**
     * Static message, should not use concatenation
     *
     * @param condition to check
     * @param message   to display if condition is false
     */
    public static void assert_(boolean condition, String message) {
        assert_(condition, () -> message);
    }

    /**
     * Static message, should not use concatenation
     *
     * @param conditionSupplier to check -- this is only executed when assertions are enabled
     * @param message           to display if condition is false
     */
    public static void assert_(BooleanSupplier conditionSupplier, String message) {
        assert_(conditionSupplier, () -> message);
    }

    /**
     * Allows to share a common computed object between condition and message supplier.
     *
     * @param supplier   of any object to be checked
     * @param condition  checks the objects (and optionally also other things)
     * @param messageFun uses the object (and optionally also other things) to create a message
     * @param <T>        type of intermediate object
     */
    public static <T> void assert_(Supplier<T> supplier, Predicate<T> condition, Function<T, String> messageFun) {
        if (!assert_enabled()) return;
        T t = supplier.get();
        VARY.assert_(condition.test(t), () -> messageFun.apply(t));
    }

    /**
     * Creates messages only when assertion is run
     *
     * @param condition       to check
     * @param messageSupplier to display if condition is false -- this is only executed when assertions are enabled
     */
    public static void assert_(boolean condition, Supplier<String> messageSupplier) {
        if (!assert_enabled()) return;
        VARY.assert_(condition, messageSupplier);
    }

    /**
     * Creates messages only when assertion is run
     *
     * @param conditionSupplier to check -- this is only executed when assertions are enabled
     * @param messageSupplier   to display if condition is false -- this is only executed when assertions are enabled
     */
    public static void assert_(BooleanSupplier conditionSupplier, Supplier<String> messageSupplier) {
        if (!assert_enabled()) return;
        VARY.assert_(conditionSupplier.getAsBoolean(), messageSupplier);
    }

    /**
     * @return true if 'assert_' assertions are enabled (must be active with compile time flag and a runtime flag)
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean assert_enabled() {
        //noinspection ConstantValue
        return ASSERTIONS_COMPILED_IN && assertionsRuntimeEnabled;
    }

    public static <T extends Throwable> void assert_throw(boolean condition, Supplier<T> supplier) throws T {
        if (!assert_enabled()) return;
        VARY.assert_throw(condition, supplier);
    }

    public static <T extends Throwable> void assert_throw(BooleanSupplier conditionSupplier, Supplier<T> supplier) throws T {
        if (!assert_enabled()) return;
        VARY.assert_throw(conditionSupplier.getAsBoolean(), supplier);
    }

    public static <R, T extends Throwable> void assert_throw(Supplier<R> resultSupplier, Predicate<R> condition, Function<R, T> supplier) throws T {
        if (!assert_enabled()) return;
        R result = resultSupplier.get();
        VARY.assert_throw(condition.test(result), () -> supplier.apply(result));
    }

    /**
     * Always throws an {@link AssertionError} with a message that this code should never be reached.
     *
     * @param <T> is just used to fake any result type
     * @return nothing, ever
     * @throws AssertionError (always!)
     */
    public static <T> T neverHappens() throws AssertionError {
        throw new AssertionError(Emoji.COLLISION + " This never happens. " + Emoji.COLLISION);
    }

    /**
     * Always throws an {@link AssertionError} with a message that this code should never be reached.
     *
     * @param msgSupplier to provide a message
     * @param <T>         is just used to fake any result type
     * @return nothing, ever
     * @throws AssertionError (always!)
     */
    public static <T> T neverHappens(Supplier<String> msgSupplier) throws AssertionError {
        throw new AssertionError(Emoji.COLLISION + " This never happens. " + Emoji.COLLISION + "\n" + msgSupplier.get());
    }

    /**
     * Always throws an {@link AssertionError}
     *
     * @param <T> is just used to fake any result type
     * @throws AssertionError (always!)
     */
    public static <T> void neverHappens(T value) throws AssertionError {
        throw new AssertionError(Emoji.COLLISION + " This never happens. " + Emoji.COLLISION);
    }

}
