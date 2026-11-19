package com.graphinout.base.gio;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class GioFileFormat {

    /** Allowed syntax of {@link #id()}. */
    public static final String ID_PATTERN = "[a-z0-9_-]+";

    /**
     * An ID unique in GIO, matching {@link #ID_PATTERN}: lowercase letters, digits, {@code _} and {@code -}, e.g.
     * {@code graphml} or {@code connected-json}. Users and the CLI select formats by this id.
     */
    private final String id;
    /**
     * A human-readable name
     */
    private final String label;
    private final Set<String> fileExtensions;

    /**
     * @param mainExtension the one used for output file naming (and as input), syntax: ".foo" (with dot)
     * @param extensions    further handled file extensions (optional), should be listed as ".foo" (with dot)
     */
    public GioFileFormat(String id, String label, String mainExtension, String... extensions) {
        this.id = id;
        this.label = label;
        if (extensions == null) this.fileExtensions = Set.of(mainExtension);
        else {
            this.fileExtensions = new LinkedHashSet<>();
            this.fileExtensions.add(mainExtension);
            this.fileExtensions.addAll(Arrays.asList(extensions));
        }
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

    public String mainExtension() {
        return fileExtensions.iterator().next();
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
