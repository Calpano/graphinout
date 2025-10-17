package com.graphinout.foundation.input;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FilesMultiInputSource implements MultiInputSource {

    private Map<String, FileSingleInputSource> namedFiles = new HashMap<>();

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
        return this.namedFiles.get(name);
    }

    @Override
    public String name() {
        return new ArrayList<>(this.namedFiles.keySet()).toString();
    }

    @Override
    public Set<String> names() {
        return null;
    }

    public FilesMultiInputSource withFile(String name, File file) {
        FileSingleInputSource f = new FileSingleInputSource(file);
        this.namedFiles.put(name, f);
        return this;
    }

}
