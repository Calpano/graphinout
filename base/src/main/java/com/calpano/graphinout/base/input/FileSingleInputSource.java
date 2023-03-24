package com.calpano.graphinout.base.input;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

@Slf4j
public class FileSingleInputSource implements SingleInputSource {

    final File file;
    private final FileInputStream inputStream;
    Optional<Charset> encoding;

    public FileSingleInputSource(File file, Charset encoding) {
        this(file, Optional.of(encoding));
    }

    /**
     * Unknown encoding
     */
    public FileSingleInputSource(File file) {
        this(file, Optional.empty());

    }

    FileSingleInputSource(File file, Optional<Charset> encoding) {
        this.file = file;
        this.encoding = encoding;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<Charset> encoding() {
        return encoding;
    }

    @Override
    public Optional<String> inputFormat() {
        return Optional.empty();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void close() throws Exception {
        log.debug("Closed inputStream <{}> type <{}>.", name(), inputStream().getClass().getName());
        inputStream.close();
    }
}
