package com.graphinout.foundation.pure.functional;

import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;

import static com.graphinout.foundation.pure.assertions.Assertions.assert_;

public class CurrentAndNext<T> {

    private @Nullable BiPredicate<T, T> equalsPredicate;
    private @Nullable T current;
    private @Nullable T next;

    public static <T> CurrentAndNext<T> create() {
        return new CurrentAndNext<>();
    }

    public static <T> CurrentAndNext<T> create(BiPredicate<T, T> equalsPredicate) {
        CurrentAndNext<T> currentAndNext = new CurrentAndNext<>();
        currentAndNext.equalsPredicate = equalsPredicate;
        return currentAndNext;
    }

    public void current(@Nullable T current) {
        this.current = current;
    }

    public @Nullable T current() {
        return current;
    }

    public boolean isSet() {
        return current != null;
    }

    public boolean isUpdateAvailable() {
        return next != null && (equalsPredicate != null ? !equalsPredicate.test(current, next) : !next.equals(current));
    }

    public @Nullable T mostRecentValue() {
        return next != null ? next : current;
    }

    /**
     * Sets the next value and returns true if the value has changed.
     */
    public boolean next(@Nullable T next) {
        this.next = next;
        return isUpdateAvailable();
    }

    public @Nullable T next() {
        return next;
    }

    public void update() {
        assert_(next != null, "next is null");
        current = next;
        next = null;
    }

}
