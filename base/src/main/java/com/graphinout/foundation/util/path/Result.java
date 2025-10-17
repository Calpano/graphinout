package com.graphinout.foundation.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a path resolution operation.
 * <p>
 * A {@code Result} encapsulates the path taken to reach a certain value within a data structure, as well as the
 * sequence of intermediate values encountered along that path.
 */
public class Result {

    private final List<Object> path;
    /** one longer than path */
    private final List<Object> values;

    /**
     * Constructs a new Result.
     *
     * @param path   the list of path segments leading to the value.
     * @param values the list of values encountered along the path, with the final value as the last element.
     */
    public Result(List<Object> path, List<Object> values) {
        this.path = path;
        this.values = values;
    }

    /**
     * Concatenates two lists.
     *
     * @param a the first list.
     * @param b the second list.
     * @return a new list containing all elements of {@code a} followed by all elements of {@code b}.
     */
    public static List<Object> concat(List<Object> a, List<Object> b) {
        ArrayList<Object> result = new ArrayList<>(a.size() + b.size());
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    /**
     * @return all directChildren results, but each 'prefixed' by root result
     */
    public static List<Result> concatResults(Result root, List<Result> directChildren) {
        List<Result> results = new ArrayList<>(directChildren.size());
        for (Result directChild : directChildren) {
            List<Object> path = concat(root.path(), directChild.path());
            List<Object> values = concat(root.values(), directChild.values());
            Result result = of(path, values);
            results.add(result);
        }
        return results;
    }

    /**
     * Static factory method to create a new {@link Result}.
     *
     * @param path   the list of path segments.
     * @param values the list of values.
     * @return a new {@link Result} instance.
     */
    public static Result of(List<Object> path, List<Object> values) {
        return new Result(path, values);
    }

    /**
     * Creates a {@link Result} representing the root of a resolution, with an empty path.
     *
     * @param value the root object.
     * @return a new {@link Result} instance for the root.
     */
    public static Result ofRoot(Object value) {
        return of(List.of(), List.of(value));
    }

    /**
     * Creates a {@link Result} representing a single resolution step.
     *
     * @param step  the path segment for this step.
     * @param value the value found at this step.
     * @return a new {@link Result} instance for the step.
     */
    public static Result ofStep(String step, Object value) {
        assert step != null;
        assert value != null;
        return of(List.of(step), List.of(value));
    }

    /**
     * @return path = (this, step); values = ( this, directChildren)
     */
    public List<Result> append(String step, List<Object> directChildren) {
        List<Result> results = new ArrayList<>(directChildren.size());
        for (Object directChild : directChildren) {
            List<Object> path = new ArrayList<>(path());
            path.add(step);
            List<Object> values = new ArrayList<>(values());
            values.add(directChild);
            Result result = of(path, values);
            results.add(result);
        }
        return results;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Result result)) return false;

        return Objects.equals(path, result.path) && Objects.equals(values, result.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(path);
        result = 31 * result + Objects.hashCode(values);
        return result;
    }

    /**
     * Gets the path taken to reach this result.
     *
     * @return the list of path segments.
     */
    public List<Object> path() {
        return path;
    }

    /**
     * Gets the final value of the resolution.
     *
     * @return the last value in the values list.
     */
    public Object value() {
        return values.getLast();
    }

    /**
     * Gets the sequence of all values encountered along the path to this result.
     *
     * @return the list of values.
     */
    public List<Object> values() {
        return values;
    }

}
