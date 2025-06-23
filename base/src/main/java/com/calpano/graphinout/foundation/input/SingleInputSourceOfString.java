package com.calpano.graphinout.foundation.input;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SingleInputSourceOfString extends ByteArrayInputSource implements SingleInputSource {


    public SingleInputSourceOfString(String name, String content) {
        super(name, content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.of(StandardCharsets.UTF_8);
    }


}
