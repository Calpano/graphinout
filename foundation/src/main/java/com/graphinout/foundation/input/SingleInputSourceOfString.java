package com.graphinout.foundation.input;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SingleInputSourceOfString extends ByteArrayInputSource implements SingleInputSource {


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


}
