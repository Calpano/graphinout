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
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
         * Optional init: record the expected result to a file. Add it to git. This way, the actual result written to a
         * file profits from git diff.
         */
        RecordInitWithExpected,
        /** record the real, actual result into a file as expected. */
        RecordTheActual
    }

    public static final String EXPECTED = "EXPECTED";
    public static final String RECORD_MODE = "RECORD_MODE";
    public static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final Logger log = getLogger(TestFileUtil.class);
    public static final String EMOJI_VIDEOCASSETTE = "\uD83D\uDCFC";
    static Pattern INVALID_MARKER = Pattern.compile("invalid([a-z]*).*");

    private static File asSrcMainResource(File targetTestClasses) {
        assert targetTestClasses.getAbsolutePath().contains(TARGET_TEST_CLASSES);
        return new File(targetTestClasses.getAbsolutePath().replace(TARGET_TEST_CLASSES, "src/test/resources"));
    }

    /**
     * @throws IllegalArgumentException if resource came from a JAR
     */
    private static File expectedFile(Resource resource, String testId) throws IllegalArgumentException {
        URI resourceUri = resource.getURI();
        if (resourceUri.toString().startsWith("jar")) {
            throw new IllegalArgumentException("Cannot map from JAR " + resourceUri);
        }
        String taggedResourceUri = tagResourcePath(resourceUri.toString(), expectedTag(testId));

        // auto-fix: if we loaded the file from some <foo>/target/test-classes/<bar>
        // there should be the original resource in <foo>/src/test/resources/<bar>
        if (taggedResourceUri.contains(TARGET_TEST_CLASSES)) {
            File checkFile =
                    asSrcMainResource(new File(URI.create(resourceUri.toString())));
            assert checkFile.isFile();
            assert checkFile.exists();

            URI taggedUri = URI.create(taggedResourceUri);
            File f = new File(taggedUri);
            return asSrcMainResource(f);
        } else {
            URI taggedUri = URI.create(taggedResourceUri);
            return new File(taggedUri);
        }
    }

    public static @Nullable Resource expectedResource(Resource resource, String testId) {
        return tagResource(resource, expectedTag(testId));
    }

    private static String expectedTag(String testId) {
        return EXPECTED + "-" + testId;
    }

    /**
     * A local file for reading/getting parent dir etc.
     *
     * @return null if the resource was loaded from a JAR
     */
    public static @Nullable File file(Resource resource) {
        assert resource != null : "resource is null";
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

    public static boolean isExpected(Resource resource) {
        String tags = tags(resource);
        return tags.startsWith(EXPECTED+"-");
    }

    /**
     * A file can be an invalid file, an invalid XML file or an invalid GraphML file but a valid XML file. So the
     * invalidness can be quantified with a marker.
     * <p>
     * Does the file start with 'invalidXML-' OR with any other marker instead of XML?
     * <p>
     * TODO switch to use -INVALID as marker at end of file name.
     *
     * @param markers can optionally accept only some
     * @return true if the path is marked as INVALID
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
        String tagsLower = tags.toLowerCase(Locale.ROOT);
        for (String marker : markers) {
            String search = "invalid"+marker.toLowerCase(Locale.ROOT);
            if (tagsLower.contains(search)) {
                return true;
            }
        }
        return false;
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
        if (Objects.equals("init", mode.toLowerCase())) {
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

    /**
     * @param resourcePath in syntax 'foo/bar/baz.buz/dingo.graphml.xml'
     * @param tag          to add, e.g. 'MYTAG'. Case-sensitive! Tags without a hyphen '-' in the name are UPPERCASED.
     * @return 'foo/bar/baz.buz/dingo--MYTAG.graphml.xml'
     */
    static String tagResourcePath(String resourcePath, String tag) {
        String tagNormalized = tag.contains("-") ? tag :
                tag.toUpperCase(Locale.ROOT);
        String name = findStrip(resourcePath, p -> p.lastIndexOf('/'), String::substring);
        String pathWithoutName = findStrip(resourcePath, p -> p.lastIndexOf('/'), (s, i) -> s.substring(0, i));
        String nameWithoutExt = findStrip(name, n -> n.indexOf('.'), (s, i) -> s.substring(0, i));
        String extensionIncludingDot = findStrip(name, n -> n.indexOf('.'), String::substring);
        return pathWithoutName + nameWithoutExt + "--" + tagNormalized + extensionIncludingDot;
    }

    private static String tags(Resource resource) {
        String resourcePath = resource.getPath();
        // foo--TAG.my.ext
        String taggedName = findStrip(resourcePath, p -> p.lastIndexOf('/'), String::substring);
        String taggedNameWithoutExt = findStrip(taggedName, n -> n.indexOf('.'), (s, i) -> s.substring(0, i));
        return findStrip(taggedNameWithoutExt, t -> t.indexOf("--"), (s, i) -> s.substring(i + 2));
    }

    /**
     * @param resource        which was initially loaded in the test
     * @param testId
     * @param actualString    what some process created as a result
     * @param expectedString  what we expect to see
     * @param actual_expected Called in test mode, but not in record mode. Is comparing actual with expected to decide
     *                        if test passes.
     * @param normalizerFun   normalizes string before writing to disk as EXPECTED or before comparing
     */
    public static void verifyOrRecord(Resource resource, String testId, String actualString, @Nullable String expectedString, BiPredicate<String, String> actual_expected, Function<String, String> normalizerFun) throws IOException {

        @Nullable Resource expectedResource = TestFileUtil.expectedResource(resource, testId);
        switch (TestFileUtil.recordMode()) {
            case Off -> {
                // if not in RECORD_MODE, read EXPECTED from tag file 'filePath--EXPECTED' and compare
                if (expectedResource != null) {
                    log.info(EMOJI_VIDEOCASSETTE + " Loaded expected output from " +expectedResource.getPath());
                    String expectedStringFromFile = expectedResource.getContentAsString();
                    // maybe normalizer function changed slightly, so normalize again
                    String expectedNorm = normalizerFun.apply(expectedStringFromFile);
                    String actualNorm = normalizerFun.apply(actualString);
                    boolean ok = actual_expected.test(actualNorm, expectedNorm);
                    if (!ok) fail();
                } else {
                    log.info(EMOJI_VIDEOCASSETTE +" You can use env RECORD_MODE= { 'init' | 'on' } as { EXPECTED | ACTUAL } result.");
                    // check
                    String expectedNorm = normalizerFun.apply(expectedString);
                    String actualNorm = normalizerFun.apply(actualString);
                    boolean ok = actual_expected.test(actualNorm, expectedNorm);
                    if (!ok) fail();
                }
            }
            case RecordInitWithExpected -> {
                // File can be put in git.
                // Test passes.
                if (expectedString != null) {
                    File f = TestFileUtil.expectedFile(resource, testId);
                    String expectedNorm = normalizerFun.apply(expectedString);
                    FileUtils.writeStringToFile(f, expectedNorm, StandardCharsets.UTF_8);
                    log.info(EMOJI_VIDEOCASSETTE +" Wrote expected to {}", f.getAbsolutePath());
                }
            }
            case RecordTheActual -> {
                // if RECORD_MODE, write EXPECTED to filePath.expected.
                // File can be put in git.
                // Test passes.
                File f = TestFileUtil.expectedFile(resource, testId);
                String actualNorm = normalizerFun.apply(actualString);
                FileUtils.writeStringToFile(f, actualNorm, StandardCharsets.UTF_8);
                log.info(EMOJI_VIDEOCASSETTE +" Wrote actual {}", f.getAbsolutePath());
            }
        }
    }


}
