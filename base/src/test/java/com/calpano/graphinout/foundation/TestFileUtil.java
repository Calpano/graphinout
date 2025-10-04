package com.calpano.graphinout.foundation;

import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import io.github.classgraph.ClassGraph;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String EXPECTED = "EXPECTED";
    public static final String RECORD_MODE = "RECORD_MODE";
    static Pattern INVALID_MARKER = Pattern.compile("invalid([a-z]*).*");

    public static File expectedFile(Path path) {
        return tagFile(path.toFile(), EXPECTED);
    }

    public static Resource expectedResource(Resource resource) {
        return tagResource(resource, EXPECTED);
    }

    /**
     * A local file for reading/getting parent dir etc.
     * @param resource
     * @return null if resource was loaded from a JAR
     */
    public static @Nullable File file(Resource resource) {
        assert resource != null;
        URI resourceUri = resource.getURI();
        if (resourceUri.toString().startsWith("jar")) {
            return null;
        }
        return new File(resourceUri);
    }

    /**
     *
     * @param in           input
     * @param searchFun    if not -1, then subStringFun is called
     * @param subStringFun transforms a string index to a substring
     * @return either in or a part of in
     */
    private static String findStrip(String in, ToIntFunction<String> searchFun, BiFunction<String, Integer, String> subStringFun) {
        int index = searchFun.applyAsInt(in);
        if (index == -1) return in;
        return subStringFun.apply(in, index);
    }

    public static SingleInputSourceOfString inputSource(Resource resource) throws IOException {
        return SingleInputSourceOfString.of(resource.getURI().toString(), resource.getContentAsString());
    }

    /**
     * A file can be an invalid file, an invalid XML file or an invalid GraphML file but a valid XML file. So the
     * invalidness can be quantified with a marker.
     * <p>
     * Does the file start with 'invalidXML-' ? or any other marker instead of XML?
     * <p>
     * TODO switch to use -INVALID as marker at end of file name.
     *
     * @param path
     * @param markers can optionally accept only some
     * @return
     */
    public static boolean isInvalid(Path path, String... markers) {
        String fileName = path.toFile().getName().toLowerCase(Locale.ROOT);

        Matcher m = INVALID_MARKER.matcher(fileName);
        if (m.matches()) {
            if (markers == null || markers.length == 0) return true;

            String foundMarker = m.group(1);
            for (String marker : Set.of(markers)) {
                if (foundMarker.equals(marker.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInvalid(Resource resource, String... markers) {
        String tags = tags(resource);
        for (String marker : markers) {
            if (tags.toLowerCase().contains(marker.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExpected(Resource resource) {
        return tags(resource).equals(EXPECTED);
    }

    public static Path path(Resource resource) {
        return Path.of(resource.getPath());
    }

    public static RecordMode recordMode() {
        String mode = System.getenv(RECORD_MODE);
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

    public static @Nullable Resource resource(String path) {
        return new ClassGraph().scan().getResourcesWithPath(path).stream().findFirst().orElse(null);
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
     * @param resource e.g. "foo/bar/baz.buz/dingo.graphml.xml"
     * @param tag      e.g. "EXPECTED"
     * @return the resource called, e.g., "foo/bar/baz.buz/dingo--EXPECTED.graphml.xml", or null if it does not exist
     */
    public static @Nullable Resource tagResource(Resource resource, String tag) {
        String path = resource.getPath();
        String taggedPath = tagResourcePath(path, tag);
        return resource(taggedPath);
    }

    static String tagResourcePath(String resourcePath, String tag) {
        String tagUpper = tag.toUpperCase(Locale.ROOT);
        String name = findStrip(resourcePath, p -> p.lastIndexOf('/'), String::substring);
        String pathWithoutName = findStrip(resourcePath, p -> p.lastIndexOf('/'), (s, i) -> s.substring(0, i));
        String nameWithoutExt = findStrip(name, n -> n.indexOf('.'), (s, i) -> s.substring(0, i));
        String extensionIncludingDot = findStrip(name, n -> n.indexOf('.'), String::substring);
        return pathWithoutName + nameWithoutExt + "--" + tagUpper + extensionIncludingDot;
    }

    private static String tags(Resource resource) {
        String resourcePath = resource.getPath();
        // foo--TAG.my.ext
        String taggedName = findStrip(resourcePath, p -> p.lastIndexOf('/'), String::substring);
        String taggedNameWithoutExt = findStrip(taggedName, n -> n.indexOf('.'), (s, i) -> s.substring(0, i));
        return findStrip(taggedNameWithoutExt, t -> t.indexOf("--"), (s, i) -> s.substring(i + 2));
    }

    /**
     * @param resource
     * @param actualString
     * @param expectedString
     * @param actual_expected Called in test mode, but not in record mode
     * @throws IOException
     */
    public static void verifyOrRecord(Resource resource, String actualString, @Nullable String expectedString, BiPredicate<String, String> actual_expected) throws IOException {

        Resource expectedResource = TestFileUtil.expectedResource(resource);
        switch (TestFileUtil.recordMode()) {
            case Off -> {
                // if not in RECORD_MODE, read EXPECTED from tag file 'filePath--EXPECTED' and compare
                if (expectedResource != null) {
                    String expectedStringFromFile = resource.getContentAsString();
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
                    File f = file( expectedResource);
                    FileUtils.writeStringToFile(f, expectedString, StandardCharsets.UTF_8);
                    log.info("Wrote expected to " + f.getAbsolutePath());
                }
            }
            case RecordTheActual -> {
                // if RECORD_MODE, write EXPECTED to filePath.expected.
                // File can be put in git.
                // Test passes.
                File f = file( expectedResource);
                FileUtils.writeStringToFile(f, actualString, StandardCharsets.UTF_8);
                log.info("Wrote actual " + f.getAbsolutePath());
            }
        }
    }

}
