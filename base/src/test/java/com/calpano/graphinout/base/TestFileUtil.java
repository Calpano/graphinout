package com.calpano.graphinout.base;

import java.nio.file.Path;
import java.util.Locale;

public class TestFileUtil {

    public static boolean isInvalid(Path path) {
        return path.toFile().getName().toLowerCase(Locale.ROOT).startsWith("invalid-");
    }

}
