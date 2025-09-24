package com.calpano.graphinout.foundation.util;

import java.util.Arrays;
import java.util.LinkedList;

public class PowerStackEnum<E extends Enum<E>> {

    /** this allows us to peek beyond the top. Top of stack is first in the list. */
    private final LinkedList<E> stack = new LinkedList<>();

    public static <E extends Enum<E>> PowerStackEnum<E> create() {
        return new PowerStackEnum<>();
    }

    /**
     * Peeks the top element and expects it to be one of the given values.
     *
     * @param expectedEnumValues  if empty, all values are accepted.
     * @throws IllegalStateException if the stack is empty or the top element is not from the expected values.
     */
    @SafeVarargs
    public final E peek(E... expectedEnumValues) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        E top = stack.peek();
        if (expectedEnumValues.length == 0) {
            return top;
        }
        for (E expected : expectedEnumValues) {
            if (top == expected) {
                return top;
            }
        }
        throw new IllegalStateException("Expected one of " + Arrays.toString(expectedEnumValues) + " but was " + top);
    }

    /**
     * Peeks from the top all elements of the stack and returns first of matching value.
     *
     * @throws IllegalStateException if the stack is empty or if no element of the given value is found.
     */
    public E peekSearch(E enumValue) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        for (E element : stack) {
            if (enumValue == element) {
                return element;
            }
        }
        throw new IllegalStateException("No element  " + enumValue + " found.");
    }

    /**
     * Pops the top element and expects it to be one of the given values.
     *
     * @throws IllegalStateException if the stack is empty or the top element is not from the expected values.
     */
    @SafeVarargs
    public final E pop(E... expectedEnumValues) throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No element to peek.");
        }
        E top = stack.pop();
        if(expectedEnumValues.length == 0) {
            return top;
        }
        for (E expected : expectedEnumValues) {
            if (top == expected) {
                return top;
            }
        }
        throw new IllegalStateException("Expected one of " + Arrays.toString(expectedEnumValues) + " but was " + top);
    }

    /** @return the added element. */
    public E push(E element) {
        stack.push(element);
        return element;
    }

}
