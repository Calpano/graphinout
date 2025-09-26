package com.calpano.graphinout.foundation;

import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.input.SingleInputSourceOfString.inputSource;

/**
 * Use as <code>@ParameterizedTest(name = "{index}: {0}")</code>
 */
public class TestFileProvider {

    public static final String SRC_TEST_RESOURCES = "../base/src/test/resources/";
    public static final Set<String> FILTER_GRAPHML = Set.of(".graphml.xml", ".graphml");
    public static final Set<String> EXTENSIONS_CJ_JSON = Set.of(".cj.json", ".cj");
    static final List<SingleInputSourceOfString> jsonInputs = List.of( //
            inputSource("number", "{\"foo\":42}"),//
            inputSource("string", "{\"foo\":\"bar\"}"),//
            inputSource("boolean-true", "{\"foo\":true}"),//
            inputSource("boolean-false", "{\"foo\":false}"),//
            inputSource("null", "{\"foo\":null}"),//
            inputSource("array-of-numbers", "{\"foo\":[1,2,3]}"),//
            inputSource("nested-object", "{\"foo\":{\"bar\":42}}"),//
            inputSource("empty-array", "[]"), //
            inputSource("empty-object", "{}"),//
            inputSource("mixed-array", "[1,\"two\",true,null,{},[]]"), //
            inputSource("two-properties", "{\"foo\":42,\"bar\":\"baz\"}"));
    /** Testing JSON 5 extensions */
    static final List<SingleInputSourceOfString> inputsJson5 = List.of(
            // == Objects
            // test: Object keys may be an ECMAScript 5.1 IdentifierName.
            inputSource("object-identifier-name", "{foo:42}"),
            // test: Objects may have a single trailing comma.
            inputSource("object-trailing-comma", "{\"foo\":42,}"),
            // == Arrays
            // test: Arrays may have a single trailing comma.
            inputSource("array-trailing-comma", "[1,2,3,]"),
            // == Strings
            // test: Strings may be single quoted.
            inputSource("single-quoted-string", "{'foo':'bar'}"),
            // test: Strings may span multiple lines by escaping new line characters.
            inputSource("multiline-string", "{\"foo\":\"line1\\\nline2\"}"),
            // test: Strings may include character escapes.
            inputSource("escaped-chars", "{\"foo\":\"\\u0041\\t\\r\\n\"}"),
            // == Numbers
            // test: Numbers may be hexadecimal.
            // TODO not yet suported:
            // inputSource("hex-number", "{\"foo\":0xFF}"),
            // test: Numbers may have a leading or trailing decimal point.
            inputSource("decimal-point", "{\"foo\":.5,\"bar\":5.}"),
            // test: Numbers may be IEEE 754 positive infinity, negative infinity, and NaN.
            inputSource("special-numbers", "{\"foo\":Infinity,\"bar\":-Infinity,\"baz\":NaN}"),
            // test: Numbers may begin with an explicit plus sign.
            inputSource("plus-sign", "{\"foo\":+42}"),
            // ==  Comments
            // test: Single and multi-line comments are allowed.
            inputSource("comments", "{\"foo\":42} // comment\n/* multi\nline */"),
            // == White Space
            // test: Additional white space characters are allowed.
            inputSource("whitespace", "{\t\"foo\"\t:\t42\t}\n"));

    /** Canonical and extended */
    public static Stream<Arguments> cjFilesAll() throws Exception {
        return files(SRC_TEST_RESOURCES + "json/cj", EXTENSIONS_CJ_JSON);
    }

    /** Only canonical */
    public static Stream<Arguments> cjFilesCanonical() throws Exception {
        return files(SRC_TEST_RESOURCES + "json/cj/canonical", EXTENSIONS_CJ_JSON);
    }

    /** Only extended */
    public static Stream<Arguments> cjFilesExtended() throws Exception {
        return files(SRC_TEST_RESOURCES + "json/cj/canonical", EXTENSIONS_CJ_JSON);
    }

    public static Stream<Arguments> cjResourcesCanonical() throws Exception {
        return resources("json/cj/canonical", EXTENSIONS_CJ_JSON);
    }

    /**
     * @param resourceRootPath e.g. "src/test/resources/json/cj/canonical"
     * @return arguments (name, path)
     */
    @SuppressWarnings("resource")
    private static Stream<Arguments> files(String resourceRootPath, Set<String> allowedExtensions) throws IOException {
        Path testResourcesPath = Paths.get(resourceRootPath);
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile) //
                .filter(p -> hasExtension(allowedExtensions.toArray(new String[0])).test(p.toString())) //
                .map(p -> Arguments.of( //
                        // pretty name
                        p.toString().substring(baseLen).replace('\\', '/'), //
                        p));
    }

    /**
     * Use the classpath resource mechanism to list ALL resources on the current classpath. Resulting paths have the
     * syntax 'com/example/filename.ext'.
     */
    public static Stream<String> getAllTestResourcePaths() {
        return new ClassGraph().scan().getAllResources().stream() //
                .map(Resource::getPath) //
                .filter(path -> !path.endsWith(".class"));
    }

    public static Stream<Arguments> graphmlFiles() throws Exception {
        return files(SRC_TEST_RESOURCES + "xml/graphml", FILTER_GRAPHML);
    }

    public static Stream<String> graphmlResourcePaths() {
        return getAllTestResourcePaths().filter(path -> path.endsWith(".graphml"));
    }

    public static Stream<String> graphmlResources() {
        return getAllTestResourcePaths().filter(p -> FILTER_GRAPHML.stream().anyMatch(ext -> p.toLowerCase().endsWith(ext.toLowerCase())));
    }

    private static Predicate<String> hasExtension(String... extensions) {
        return p -> {
            String pathName = p.toLowerCase();
            for (String ext : extensions) {
                if (pathName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Stream<Arguments> json5InputSources() {
        return inputsJson5.stream().map(is -> Arguments.of(is.name(), is));
    }

    public static Stream<Arguments> jsonFiles() throws Exception {
        return files(SRC_TEST_RESOURCES + "json", Set.of(".json"));
    }

    public static Stream<SingleInputSourceOfString> jsonInputSources() {
        return jsonInputs.stream();
    }

    private static Stream<Arguments> resources(String resourceRootPath, Set<String> allowedExtensions) throws IOException {
        Path testResourcesPath = Paths.get(resourceRootPath);
        int baseLen = testResourcesPath.toString().length() + 1;
        return getAllTestResourcePaths() //
                .filter(path -> path.startsWith(resourceRootPath))//
                .filter(path -> hasExtension(allowedExtensions.toArray(new String[0])).test(path)) //
                .map(p -> Arguments.of( //
                        // pretty name
                        p.substring(baseLen).replace('\\', '/'), //
                        p));
    }

    /** includes all graphml files */
    public static Stream<Arguments> xmlFiles() throws Exception {
        return files(SRC_TEST_RESOURCES + "xml", Set.of(".xml", ".graphml"));
    }

}
