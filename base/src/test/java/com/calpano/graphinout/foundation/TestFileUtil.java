package com.calpano.graphinout.foundation;

import io.github.classgraph.Resource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;

import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class TestFileUtil {

    public enum RecordMode {
        /** default */
        Off,
        /**
         * optional init: record the expected result to a file. Add it to git. This way, the actual result written to a
         * file profits from git diff.
         */
        RecordInitWithExpected,
        /** record the real, actual result into a file as expected. */
        RecordTheActual
    }

    private static final Logger log = getLogger(TestFileUtil.class);

    public static File expectedFile(Path path) {
        return tagFile(path.toFile(), "EXPECTED");
    }

    public static File expectedFile(Resource resource) {
        URI uri = resource.getURI();
        throw new UnsupportedOperationException("not yet impl");
//        return new File( f.getParentFile(), f.getName()+".EXPECTED");
    }

    /**
     * TODO switch to use -INVALID as marker at end of file name.
     *
     * @param path
     * @return
     */
    public static boolean isInvalid(Path path) {
        return path.toFile().getName().toLowerCase(Locale.ROOT).startsWith("invalid-");
    }

    public static RecordMode recordMode() {
        String mode = System.getenv("RECORD_MODE");
        if (mode == null || Set.of("off", "false", "no", "0").contains(mode.toLowerCase())) {
            return RecordMode.Off;
        }
        if (Set.of("on", "true", "yes", "1").contains(mode.toLowerCase())) {
            return RecordMode.RecordTheActual;
        }
        if (Set.of("init").contains(mode.toLowerCase())) {
            return RecordMode.RecordInitWithExpected;
        }
        return RecordMode.Off;
    }

    /**
     * @param f   a file which may or may not exist
     * @param tag is automatically turned into uppercase
     * @return For a file named /foo/bar/baz.ext and tag META return the file /foo/bar/baz-META.ext
     */
    public static File tagFile(File f, String tag) {
        String name = f.getName();
        String parent = f.getParent();
        int dot = name.lastIndexOf('.');
        String tagUpper = tag.toUpperCase(Locale.ROOT);
        if (dot == -1) {
            return new File(parent, name + "--" + tagUpper);
        }
        String nameWithoutExt = name.substring(0, dot);
        String ext = name.substring(dot);
        return new File(parent, nameWithoutExt + "--" + tagUpper + ext);
    }

    /**
     * @param filePath
     * @param actualString
     * @param expectedString
     * @param actual_expected Called in test mode, but not in record mode
     * @throws IOException
     */
    public static void verifyOrRecord(Path filePath, String actualString, @Nullable String expectedString, BiPredicate<String, String> actual_expected) throws IOException {

        File expectedFile = TestFileUtil.expectedFile(filePath);
        switch (TestFileUtil.recordMode()) {
            case Off -> {
                // if not in RECORD_MODE, read EXPECTED from tag file 'filePath--EXPECTED' and compare
                if (expectedFile.exists()) {
                    String expectedStringFromFile = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
                    boolean ok = actual_expected.test(actualString, expectedStringFromFile);
                    if (!ok) fail();
                } else {
                    log.info("Use env RECORD_MODE= { 'init' | 'on' } as { EXPECTED | ACTUAL } result.");
                    // check
                    boolean ok = actual_expected.test(actualString, expectedString);
                    if (!ok) fail();
                }
            }
            case RecordInitWithExpected -> {
                // File can be put in git.
                // Test passes.
                if (expectedString != null) {
                    FileUtils.writeStringToFile(expectedFile, expectedString, StandardCharsets.UTF_8);
                    log.info("Wrote expected to " + expectedFile.getAbsolutePath());
                }
            }
            case RecordTheActual -> {
                // if RECORD_MODE, write EXPECTED to filePath.expected.
                // File can be put in git.
                // Test passes.
                FileUtils.writeStringToFile(expectedFile, actualString, StandardCharsets.UTF_8);
                log.info("Wrote actual " + expectedFile.getAbsolutePath());
            }
        }
    }

}
