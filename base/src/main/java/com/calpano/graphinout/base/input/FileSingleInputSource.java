package com.calpano.graphinout.base.input;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

@Slf4j
public class FileSingleInputSource implements SingleInputSource {

    final File file;
    private final FileInputStream inputStream;
    @Nullable
    Charset encoding;


    /**
     * Unknown encoding
     */
    public FileSingleInputSource(File file) {
        this(file, null);

    }

    public FileSingleInputSource(File file, @Nullable Charset encoding) {
        this.file = file;
        this.encoding = encoding;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        log.debug("Closed inputStream <{}> type <{}>.", name(), inputStream().getClass().getName());
        inputStream.close();
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.ofNullable(encoding);
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
        try {
            return this.file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
