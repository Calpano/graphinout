package com.graphinout.foundation.jajson;

import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class JaJsonMapBuilder {

    private final Map<String, Object> map;

    JaJsonMapBuilder() {
        this.map = new LinkedHashMap<>();
    }

    public Map<String, Object> build() {
        return map;
    }

    public JaJsonMapBuilder put(String key, @NonNull Object value) {
        assert JaJson.isJaJson(value);
        map.put(key, value);
        return this;
    }

    public JaJsonMapBuilder putAll(Map<String, Object> map) {
        assert JaJson.isJaJsonMap(map);
        this.map.putAll(map);
        return this;
    }

    public <I, O> JaJsonMapBuilder putMaybe(String key, @Nullable List<I> listInput, @NonNull Function<I, O> mapFun) {
        if (listInput != null) {
            List<Object> listOutput = new ArrayList<>();
            for (I i : listInput) {
                O o = mapFun.apply(i);
                assert JaJson.isJaJson(o);
                listOutput.add(o);
            }
            map.put(key, listOutput);
        }
        return this;
    }

    public <I, O> JaJsonMapBuilder putMaybe(String key, @Nullable Stream<I> stream, @NonNull Function<I, O> mapFun) {
        if (stream != null) {
            List<O> listOutput = stream.map(i -> {
                O o = mapFun.apply(i);
                assert JaJson.isJaJson(o);
                return o;
            }).toList();
            if (!listOutput.isEmpty()) {
                map.put(key, listOutput);
            }
        }
        return this;
    }

    public <I, O> JaJsonMapBuilder putMaybe(String key, @Nullable I value, @NonNull Function<@NonNull I, O> mapFun) {
        if (value != null) {
            O o = mapFun.apply(value);
            assert JaJson.isJaJson(o);
            map.put(key, o);
        }
        return this;
    }

    public JaJsonMapBuilder putMaybe(String key, @Nullable Object value) {
        assert JaJson.isJaJson(value);
        if (value != null) map.put(key, value);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JaJsonMapBuilder putNonNull(String key, Object value) {
        assert JaJson.isJaJson(value);
        map.put(key, value);
        return this;
    }


}
