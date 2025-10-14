package com.calpano.graphinout.foundation.util;

import java.util.LinkedList;

public class PowerStackOnClasses<S> {

    /** this allows us to peek beyond the top. Top of stack is first in the list. */
    private final LinkedList<S> stack = new LinkedList<>();

    public static <S> PowerStackOnClasses<S> create() {
        return new PowerStackOnClasses<>();
    }

    public S peek() {
        return stack.peek();
    }

    /**
     * Peeks the top element and expects it to be of the given clazz.
     *
     * @throws IllegalStateException if the stack is empty or the top element is not of the given clazz.
     */
    public <T extends S> T peek(Class<T> clazz) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        S top = stack.peek();
        if (clazz.isInstance(top)) {
            return clazz.cast(top);
        }
        throw new IllegalStateException("Expected " + clazz + " but was " + top);
    }

    /**
     * Peeks from the top all elements of the stack and returns first of the given clazz.
     *
     * @throws IllegalStateException if the stack is empty or if no element of the given clazz is found.
     */
    public <T extends S> T peekSearch(Class<T> clazz) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        for (S element : stack) {
            if (clazz.isInstance(element)) {
                return clazz.cast(element);
            }
        }
        throw new IllegalStateException("No element of type " + clazz + " found.");
    }

    /**
     * Pops the top element and expects it to be of the given clazz.
     *
     * @throws IllegalStateException if the stack is empty or the top element is not of the given clazz.
     */
    public <T extends S> T pop(Class<T> clazz) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to pop.");
        }
        S top = stack.pop();
        if (clazz.isInstance(top)) {
            return clazz.cast(top);
        }
        throw new IllegalStateException("Expected " + clazz + " but top was " + top);
    }

    /** @return the added element. */
    public <T extends S> T push(T element) {
        assert element != null;
        stack.push(element);
        return element;
    }

    public void reset() {
        this.stack.clear();
    }

}
