package com.calpano.graphinout.foundation.input;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ByteArrayInputSource implements SingleInputSource {

    private final List<ByteArrayInputStream> openStreams = new ArrayList<>();
    private final byte[] bytes;
    private final String name;

    public ByteArrayInputSource(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    @Override
    public void close() throws IOException {
        for (InputStream is : openStreams) {
            is.close();
            log.debug("Closed inputStream <{}> type <{}>.", name(), is.getClass().getName());
        }
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.empty();
    }

    @Override
    public Optional<String> inputFormat() {
        return Optional.empty();
    }

    @Override
    public InputStream inputStream() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        openStreams.add(is);
        return is;
    }

    @Override
    public String name() {
        return name;
    }


}
