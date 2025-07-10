package com.calpano.graphinout.foundation.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileSingleInputSource implements SingleInputSource {

    private static final Logger log = LoggerFactory.getLogger(FileSingleInputSource.class);

    final File file;
    private final List<InputStream> inputStreams = new ArrayList<>();

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
    }

    @Override
    public void close() throws IOException {
        for (InputStream in : inputStreams) {
            log.debug("Closed inputStream <{}> type <{}>.", name(), in.getClass().getName());
            in.close();
        }
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.ofNullable(encoding);
    }

    public File file() {
        return file;
    }

    @Override
    public Optional<String> inputFormat() {
        return Optional.empty();
    }

    @Override
    public InputStream inputStream() throws IOException {
        try {
            InputStream inputStream = new FileInputStream(file);
            inputStreams.add(inputStream);
            return inputStream;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
