package com.graphinout.foundation.pure.collections;

import com.graphinout.foundation.pure.annotations.GwtCompatible;
import com.graphinout.foundation.pure.annotations.GwtIncompatible;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.graphinout.foundation.pure.bridge.JavaPlatform.Class.cast;

/**
 * Very limited functionality in j2cl.
 * @param <S>
 */
public class PowerStackOnClasses<S> {

    /** this allows us to peek beyond the top. Top of stack is first in the list. */
    private final LinkedList<S> stack = new LinkedList<>();

    public static <S> PowerStackOnClasses<S> create() {
        return new PowerStackOnClasses<>();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public S peek() {
        return stack.peek();
    }

    /**
     * Peeks the top element and expects it to be of the given clazz.
     *
     * @see #peek(Class, Predicate, Function) j2cl-friendly version
     * @throws IllegalStateException if the stack is empty or the top element is not of the given clazz.
     */
    @GwtIncompatible("Class.isInstance, cast")
    public <T extends S> T peek(Class<T> clazz) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        S top = stack.peek();
        if (clazz.isInstance(top)) {
            return cast(clazz, top);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + top);
    }

    /**
     * j2cl-friendly version of {@link #peek(Class)}
     * @param clazz
     * @param test
     * @param castFun
     * @return
     * @param <T>
     * @throws IllegalStateException
     */
    @GwtCompatible
    public <T extends S> T peek(Class<T> clazz, Predicate<S> test,Function<S,T>castFun) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        S top = stack.peek();
        if(test.test(top)) {
            return castFun.apply(top);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + top);
    }

    /**
     * Like {@link #peek(Class)} but returns null instead of throwing when the top element is absent or not of the
     * requested type.
     */
    @GwtIncompatible("Class.isInstance, cast")
    public <T extends S> T peekOrNull(Class<T> clazz) {
        if (stack.isEmpty()) return null;
        S top = stack.peek();
        return clazz.isInstance(top) ? cast(clazz, top) : null;
    }

    /**
     * Peeks from the top all elements of the stack and returns first of the given clazz.
     *
     * @throws IllegalStateException if the stack is empty or if no element of the given clazz is found.
     */
    @GwtIncompatible("Class.isInstance, cast")
    public <T extends S> T peekSearch(Class<T> clazz) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        for (S element : stack) {
            if (clazz.isInstance(element)) {
                return cast(clazz, element);
            }
        }
        throw new IllegalStateException("No element of type " + clazz + " found.");
    }

    /**
     * Like {@link #peekSearch(Class)} but returns null instead of throwing when the stack is empty or no element of the
     * requested type exists.
     */
    @GwtIncompatible("Class.isInstance, cast")
    public <T extends S> T peekSearchOrNull(Class<T> clazz) {
        if (stack.isEmpty()) return null;
        for (S element : stack) {
            if (clazz.isInstance(element)) {
                return cast(clazz, element);
            }
        }
        return null;
    }

    /**
     * Pops the top element and expects it to be of the given clazz.
     *
     * @throws IllegalStateException if the stack is empty or the top element is not of the given clazz.
     */
    @GwtIncompatible("Class.isInstance, cast")
    public <T extends S> T pop(Class<T> clazz) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to pop.");
        }
        S top = stack.pop();
        if (clazz.isInstance(top)) {
            return cast(clazz, top);
        }
        throw new IllegalStateException("Expected " + clazz + " but top was " + top + ". Most likely a top-element was not properly popped/closed.");
    }

    /** @return the added element. */
    public <T extends S> T push(T element) {
        assert element != null : "Don't push null into the stack";
        stack.push(element);
        return element;
    }

    public void reset() {
        this.stack.clear();
    }

}
