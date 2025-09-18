package com.calpano.graphinout.foundation.json.value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class JsonPaths {

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

}
