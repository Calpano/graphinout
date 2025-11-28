package com.graphinout.foundation.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ByteArrayInputSource implements SingleInputSource {

    private static final Logger log = LoggerFactory.getLogger(ByteArrayInputSource.class);

    private final List<ByteArrayInputStream> openStreams = new ArrayList<>();
    private final byte[] bytes;
    private final String name;

    public ByteArrayInputSource(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    public byte[] bytes() {
        return bytes;
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
