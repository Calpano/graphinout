package com.calpano.graphinout.foundation.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Result {

    private final List<Object> path;
    /** one longer than path */
    private final List<Object> values;

    public Result(List<Object> path, List<Object> values) {
        this.path = path;
        this.values = values;
    }

    public static List<Object> concat(List<Object> a, List<Object> b) {
        ArrayList<Object> result = new ArrayList<>(a.size() + b.size());
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public static Result of(List<Object> path, List<Object> values) {
        return new Result(path, values);
    }

    public static Result ofRoot(Object value) {
        return of(List.of(), List.of(value));
    }

    public static Result ofStep(String step, Object value) {
        assert step != null;
        assert value != null;
        return of(List.of(step), List.of(value));
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

    public List<Object> path() {
        return path;
    }

    public Object value() {
        return values.getLast();
    }

    public List<Object> values() {
        return values;
    }

}
