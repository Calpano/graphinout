package com.calpano.graphinout.foundation.util.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A NAME resolves an object property. A '$'+NAME resolves all members of an array element.
 */
class Step {

    public enum Kind {
        ListAll, List1, MapAll, Any, Map1
    }

    private final String step;

    public Step(String step) {
        this.step = step;
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
            result.add(Result.ofStep("[" + i + "]", value));
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
            assert value != null : "Found key '" + key + "' but no value.";
            result.add(Result.ofStep(key, value));
        }
        return result;
    }

    /** strip first and last char */
    public int asIndex() {
        String s = this.step.substring(1, this.step.length() - 1);
        return Integer.parseInt(s);
    }

    public Kind kind() {
        if (step.equals("..")) {
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

    public List<Result> resolve(PathResolver pr, Object root) {
        assert root != null;
        switch (kind()) {
            case List1 -> {
                // must convert to list
                IListLike list = pr.typeAdapters().adaptTo(root, IListLike.class);
                return list == null ? Collections.emptyList() : list1(list, asIndex());
            }
            case ListAll -> {
                // must convert to list
                IListLike list = pr.typeAdapters().adaptTo(root, IListLike.class);
                return list == null ? Collections.emptyList() : listAll(list);
            }
            case Map1 -> {
                // must convert to map
                IMapLike map = pr.typeAdapters().adaptTo(root, IMapLike.class);
                return map == null ? Collections.emptyList() : map1(map, step);
            }
            case MapAll -> {
                // must convert to map
                IMapLike map = pr.typeAdapters().adaptTo(root, IMapLike.class);
                return map == null ? Collections.emptyList() : mapAll(map);
            }
            case Any -> {
                return pr.resolveAny( root);
            }
            default -> throw new IllegalArgumentException("Unknown step kind: " + kind());
        }
    }

    @Override
    public String toString() {
        return "'" + step + "'(" + kind() + ")";
    }




}
