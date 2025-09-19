package com.calpano.graphinout.foundation;

import java.nio.file.Path;
import java.util.Locale;

public class TestFileUtil {

    /**
     * TODO switch to use -INVALID as marker at end of file name.
     * @param path
     * @return
     */
    public static boolean isInvalid(Path path) {
        return path.toFile().getName().toLowerCase(Locale.ROOT).startsWith("invalid-");
    }

}
