package com.graphinout.base.reader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GioFileFormat {

    /**
     * an ID unique in GIO; Only [a-z0-9_] in the id String.
     */
    private final String id;
    /**
     * A human-readable name
     */
    private final String label;
    private final Set<String> fileExtensions;

    /**
     * @param extensions handled file extensions (optional), should be listed as ".foo" (with dot)
     */
    public GioFileFormat(String id, String label, String... extensions) {
        this.id = id;
        this.label = label;
        this.fileExtensions = extensions == null ? new HashSet<>() : new HashSet<>(Arrays.asList(extensions));
    }

    public Set<String> fileExtensions() {
        return fileExtensions;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    /**
     * @param resourcePath
     * @return true iff given path ends with one of the registered file extensions
     */
    public boolean matches(String resourcePath) {
        for (String ext : fileExtensions) {
            if (resourcePath.endsWith(ext))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "'" + label + "' [" + id + "]";
    }

}
