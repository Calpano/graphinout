package com.graphinout.base.input;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SingleInputSourceOfString extends ByteArrayInputSource implements SingleInputSource, ParameterizedInputSource {

    public SingleInputSourceOfString(String name, String content) {
        super(name, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * This one is for static imports.
     */
    public static SingleInputSourceOfString inputSource(String name, String content) {
        return of(name, content);
    }


    public static SingleInputSourceOfString of(String name, String content) {
        return new SingleInputSourceOfString(name, content);
    }

    public String content() {
        return new String(bytes(), StandardCharsets.UTF_8);
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.of(StandardCharsets.UTF_8);
    }

    @Override
    public @Nullable String getValue(String key) {
        return keyValues.get(key);
    }

    @Override
    public @NonNull Set<String> keys() {
        return keyValues.keySet();
    }

    private final Map<String,String> keyValues = new HashMap<>();
}
