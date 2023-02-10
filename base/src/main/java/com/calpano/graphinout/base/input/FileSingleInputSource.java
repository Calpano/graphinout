package com.calpano.graphinout.base.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class FileSingleInputSource implements SingleInputSource {

    final File file;
    Optional<Charset> encoding;

    public FileSingleInputSource(File file, Charset encoding) {
        this.file = file;
        this.encoding = Optional.of( encoding );
    }

    /** Unknown encoding */
    public FileSingleInputSource(File file) {
        this.file = file;
        this.encoding = Optional.empty();
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
        return new FileInputStream(file);
    }

    @Override
    public String name() {
        return null;
    }
}
