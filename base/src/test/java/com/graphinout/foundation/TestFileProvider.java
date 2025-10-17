package com.graphinout.foundation;

import com.graphinout.foundation.input.SingleInputSourceOfString;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.provider.Arguments;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.graphinout.foundation.input.SingleInputSourceOfString.inputSource;

/**
 * Use as <code>@ParameterizedTest(name = "{index}: {0}")</code>
 */
public class TestFileProvider {

    public static class TestResource implements Arguments {

        final Resource resource;
        final @Nullable String label;

        public TestResource(@Nullable String label, Resource resource) {
            assert resource != null;
            this.label = label;
            this.resource = resource;
        }

        public TestResource(Resource resource) {this(null, resource);}

        public static TestResource testResource(Resource resource) {
            return new TestResource(resource);
        }

        public static TestResource testResource(String label, Resource resource) {
            return new TestResource(label, resource);
        }

        public File asFile() {
            return TestFileUtil.file(resource);
        }

        public String asPath() {
            return resource.getPath();
        }

        @Override
        public Object[] get() {
            return new Object[]{label == null ? resource.getPath() : label, resource};
        }

        /** is this resource marked with --EXPECTED ? */
        public boolean isExpected() {
            return TestFileUtil.isExpected(resource);
        }

        @Nullable
        public String label() {
            return label;
        }

        public Resource resource() {
            return resource;
        }

    }

    public static final Set<String> EXTENSIONS_GRAPHML = Set.of(".graphml.xml", ".graphml");
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

    public static Stream<TestResource> cjResourcesCanonical() {
        return resources("json/cj/canonical", EXTENSIONS_CJ_JSON);
    }

    /** Only extended */
    public static Stream<TestResource> cjResourcesExtended() {
        return resources("json/cj/canonical", EXTENSIONS_CJ_JSON);
    }

    /**
     * Use the classpath resource mechanism to list ALL resources within packages 'xml','jsom','json5' on the current
     * classpath. Resulting paths have the syntax 'com/example/filename.ext'.
     */
    public static Stream<TestResource> getAllTestResources() {
        return new ClassGraph()
                .acceptPackages("json", "xml", "json5", "text")
                .scan().getAllResources().stream() //
                .filter(res -> !res.getPath().endsWith(".class")) //
                .map(TestResource::testResource) //
                .filter(tr -> !tr.isExpected());
    }

    public static Stream<TestResource> graphmlResources() {
        return resources("xml/graphml", EXTENSIONS_GRAPHML);
    }

    private static Predicate<String> hasExtension(String... extensions) {
        return p -> {
            String pathName = p.toLowerCase();
            for (String ext : extensions) {
                if (pathName.endsWith(ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Stream<Arguments> json5InputSources() {
        return inputsJson5.stream().map(is -> Arguments.of(is.name(), is));
    }

    public static Stream<SingleInputSourceOfString> jsonInputSources() {
        return jsonInputs.stream();
    }

    public static Stream<TestResource> jsonResources() {
        return resources("json", Set.of(".json"));
    }

    public static TestResource resourceByPath(String path) {
        return resources(path, Set.of()).findFirst().orElseThrow();
    }

    /**
     * @param allowedExtensions if empty, allow all
     */
    private static Stream<TestResource> resources(String resourceRootPath, Set<String> allowedExtensions) {
        Path testResourcesPath = Paths.get(resourceRootPath);
        int baseLen = testResourcesPath.toString().length() + 1;
        return getAllTestResources() //
                .filter(tr -> tr.resource().getPath().startsWith(resourceRootPath))//
                .filter(tr -> allowedExtensions.isEmpty() || //
                        hasExtension(allowedExtensions.toArray(new String[0])).test(tr.resource().getPath())) //
                .map(res -> {
                    // pretty name
                    String name = res.resource.getPath();
                    if (name.length() > baseLen) {
                        name = name.substring(baseLen).replace('\\', '/');
                    }
                    return TestResource.testResource(name, res.resource);
                });
    }

    /** includes all graphml files */
    public static Stream<TestResource> xmlResources() {
        return resources("xml", Set.of(".xml", ".graphml"));
    }

}
