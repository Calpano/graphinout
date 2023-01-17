package com.calpano.graphinout.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class FileInputSource implements InputSource{

    final File file;
    Optional<Charset> encoding;

    public FileInputSource(File file, Charset encoding) {
        this.file = file;
        this.encoding = Optional.of( encoding );
    }

    /** Unknown encoding */
    public FileInputSource(File file) {
        this.file = file;
        this.encoding = Optional.empty();
    }


    @Override
    public Optional<Charset> encoding() {
        return encoding;
    }

    @Override
    public Optional<String> fileFormat() {
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
