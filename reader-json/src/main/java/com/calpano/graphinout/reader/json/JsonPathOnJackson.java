package com.calpano.graphinout.reader.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonPathOnJackson {

    public static class JsonPath {
        /** String = property, Integer = index */
        List<Object> steps;

        public static JsonPath of(String jsonPath) {
            JsonPath path = new JsonPath();
            // turn "aaaa.bbbb[4].ccc" into "aaaa", "bbbb", 4, "ccc"
            path.steps = Arrays.stream(jsonPath.split("\\.")).flatMap(s -> {
                if (s.endsWith("]")) {
                    String[] parts = s.split("\\[");
                    return Stream.of(parts[0], Integer.parseInt(parts[1].substring(0, parts[1].length() - 1)));
                } else {
                    return Stream.of(s);
                }
            }).collect(Collectors.toList());
            return path;
        }
    }

    public static Object runJsonPathOnJackson(JsonNode jsonNode, String jsonPath) {
        JsonPath p = JsonPath.of(jsonPath);

        JsonNode tmp = jsonNode;
        for (Object step : p.steps) {
            if (step instanceof String propertyStep) tmp = tmp.get(propertyStep);
            else if (step instanceof Integer indexStep) tmp = tmp.get(indexStep);
            else throw new AssertionError("unexpected step type");
        }

        if(tmp==null) return null;
        if(tmp.isArray()){
            //TODO
            List<Integer> list =  new ArrayList<>();
            for (final JsonNode objNode : tmp) {
                list.add(objNode.asInt());
            }
            return list;
        }else if(tmp.isTextual())
            tmp.asText();

        return "";
    }

}
