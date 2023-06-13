package com.calpano.graphinout.base.input;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class FilesMultiInputSource implements MultiInputSource {

    private Map<String,FileSingleInputSource> namedFiles = new HashMap<>();

    public FilesMultiInputSource withFile(String name, File file) {
        FileSingleInputSource f = new FileSingleInputSource(file );
        this.namedFiles.put(name, f);
        return this;
    }

    @Override
    public void close() throws Exception {
        namedFiles.values().forEach(f -> {
            try {
                f.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public SingleInputSource getNamedSource(String name) {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Set<String> names() {
        return null;
    }
}
