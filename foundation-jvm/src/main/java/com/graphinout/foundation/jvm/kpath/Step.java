package com.graphinout.foundation.jvm.kpath;

import com.graphinout.foundation.pure.collections.IListLike;
import com.graphinout.foundation.pure.collections.IMapLike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One step of a path. See path-resolver.adoc for the syntax.
 * <p>
 * A quoted step ({@code 'key'}, see {@link KPaths}) is always a literal map key ({@link Kind#Map1}).
 */
class Step {

    public enum Kind {
        ListAll, List1, MapAll, Any, Map1
    }

    private final String step;
    /** only for {@link Kind#List1} steps, else -1 */
    private final int index;

    /**
     * @throws IllegalArgumentException if the step is a list index ({@code [...]}) whose content is not a
     *                                  non-negative integer, e.g. {@code [x]}, {@code []} or {@code [-1]}
     */
    public Step(String step) {
        this.step = step;
        this.index = kind() == Kind.List1 ? parseIndex(step) : -1;
    }

    private static int parseIndex(String step) {
        String s = step.substring(1, step.length() - 1);
        if (s.isEmpty() || !s.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new IllegalArgumentException("Invalid list index step '" + step + "': expected [n] with n >= 0, [*] or [$...]. Quote it ('" + step + "') to use it as a map key.");
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid list index step '" + step + "': index too large", e);
        }
    }

    private static List<Result> list1(IListLike list, int index) {
        Object o = null;
        if (index >= 0 && index < list.size()) {
            o = list.get(index);
        }
        return o == null ? Collections.emptyList() : List.of(Result.ofStep("[" + index + "]", o));
    }

    static List<Result> listAll(IListLike list) {
        List<Result> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            // skip null elements, like list1, map1 and mapAll do; Result.ofStep rejects null
            if (value != null) {
                result.add(Result.ofStep("[" + i + "]", value));
            }
        }
        return result;
    }

    private static List<Result> map1(IMapLike map, String step) {
        Object o = map.get(step);
        return o == null ? Collections.emptyList() : List.of(Result.ofStep(step, o));
    }

    static List<Result> mapAll(IMapLike map) {
        List<String> keys = map.keys();
        List<Result> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                result.add(Result.ofStep(key, value));
            }
        }
        return result;
    }

    /** @return the index of a {@link Kind#List1} step, e.g. 7 for {@code [7]} */
    public int asIndex() {
        return index;
    }

    /** @return the map key for {@link Kind#Map1} steps, with quotes removed if the step is quoted */
    public String key() {
        return KPaths.isQuoted(step) ? KPaths.unquote(step) : step;
    }

    public Kind kind() {
        if (KPaths.isQuoted(step)) {
            return Kind.Map1;
        } else if (step.equals("..")) {
            return Kind.Any;
        } else if (step.startsWith("[") && step.endsWith("]")) {
            if (step.startsWith("[$") || step.equals("[*]")) {
                // is an array step
                return Kind.ListAll;
            } else {
                return Kind.List1;
            }
        } else {
            if (step.startsWith("$") || step.equals("*")) {
                return Kind.MapAll;
            } else {
                return Kind.Map1;
            }
        }
    }

    /**
     * Applies this step to one earlier result.
     *
     * @param from the result to continue from; its {@link Result#value()} is navigated
     * @return the matches, each extending {@code from} by the keys/indexes taken and the values passed
     */
    public List<Result> resolve(PathResolver pr, Result from) {
        if (kind() == Kind.Any) {
            return pr.descendantsOrSelf(from);
        }
        return Result.concatResults(from, resolveRelative(pr, from.value()));
    }

    /** @return results relative to {@code value}: one step in the path, the matched value in the values */
    private List<Result> resolveRelative(PathResolver pr, Object value) {
        assert value != null;
        switch (kind()) {
            case List1 -> {
                IListLike list = pr.typeAdapters().adaptTo(value, IListLike.class);
                return list == null ? Collections.emptyList() : list1(list, asIndex());
            }
            case ListAll -> {
                IListLike list = pr.typeAdapters().adaptTo(value, IListLike.class);
                return list == null ? Collections.emptyList() : listAll(list);
            }
            case Map1 -> {
                IMapLike map = pr.typeAdapters().adaptTo(value, IMapLike.class);
                return map == null ? Collections.emptyList() : map1(map, key());
            }
            case MapAll -> {
                IMapLike map = pr.typeAdapters().adaptTo(value, IMapLike.class);
                return map == null ? Collections.emptyList() : mapAll(map);
            }
            default -> throw new IllegalStateException("Unexpected step kind: " + kind());
        }
    }

    @Override
    public String toString() {
        return "'" + step + "'(" + kind() + ")";
    }


}
