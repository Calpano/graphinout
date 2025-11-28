package com.graphinout.foundation.json.value;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * JSON paths address object/array structures via steps. Each step is a String (property key) or Integer (array index).
 * Multiple steps are a path.
 */
public class JsonPaths {

    public static List<Object> ofList(Object ... jsonPathSteps) {
        return Arrays.asList(jsonPathSteps);
    }

    public static List<Object> of(Object jsonPath) {
        if (jsonPath instanceof List) {
            return (List<Object>) jsonPath;
        } else if (jsonPath instanceof Stream) {
            return ofStream(Stream.of(jsonPath));
        } else if (jsonPath instanceof String) {
            return ofPathWithDots((String) jsonPath);
        } else {
            throw new IllegalArgumentException("jsonPath must be List or Stream or String");
        }
    }

    @SafeVarargs
    public static  boolean endsWith( List<Object> path, Predicate<Object>... stepPredicates) {
        if (path.size() < stepPredicates.length) return false;
        for (int i = 0; i < stepPredicates.length; i++) {
            Object step = path.get(path.size() - stepPredicates.length + i);
            Predicate<Object> test = stepPredicates[i];
            if (!test.test(step)) return false;
        }
        return true;
    }

    /**
     * Parse a path with dots into a list of steps.
     */
    public static List<Object> ofPathWithDots(String pathWithDots) {
        return ofStream(Arrays.stream(pathWithDots.split("[.]")).map(o -> {
            if (o.matches("\\d+")) {
                return Integer.parseInt(o);
            } else {
                return o;
            }
        }));
    }

    public static List<Object> ofStream(Stream<Object> steps) {
        return steps.toList();
    }

    public static String toPathString(List<Object> steps) {
        return steps.stream().map(Object::toString).reduce((a, b) -> a + "." + b).orElse("");
    }

}
