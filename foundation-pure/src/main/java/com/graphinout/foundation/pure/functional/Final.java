package com.graphinout.foundation.pure.functional;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A mutable object that can be set only once. Getter and setter throw exceptions on invalid state. Use {@link #isSet()}
 * to check if the value is set.
 *
 * @param <T> the type of the value
 */
public class Final<T> {

    private T value;
    private boolean isSet;

    /**
     * Create a new final object with no value set.
     */
    public static <T> Final<T> empty() {
        return new Final<>();
    }

    /**
     * Get the value of this final object.
     */
    public @Nullable T get() throws IllegalStateException {
        if (!isSet) {
            throw new IllegalStateException("Final object is not set yet");
        }
        return value;
    }

    /** Get the value of this final object, or null if it is not set. */
    public @Nullable T getOrNull() {
        return isSet ? value : null;
    }

    /**
     * Check if the value of this final object is set.
     */
    public boolean isSet() {
        return isSet;
    }

    /**
     * Set the value of this final object. Throws an exception if the value was already set.
     *
     * @param value the value to set
     */
    public void set(T value) throws IllegalStateException {
        if (isSet) {
            throw new IllegalStateException("Final object is already set");
        }
        this.value = value;
        isSet = true;
    }

    /** Set the value of this final object if it is not already set. */
    public void setIfEmpty(Supplier<T> valueSupplier) {
        if (!isSet) {
            set(valueSupplier.get());
        }
    }

    /** Set the value of this final object if it is not already set, otherwise run the elseRunnable. */
    public void setOrElse(T value, Runnable elseRunnable) {
        if (!isSet) {
            set(value);
        } else {
            elseRunnable.run();
        }
    }

}
