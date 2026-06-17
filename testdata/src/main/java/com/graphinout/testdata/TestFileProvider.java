package com.graphinout.testdata;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static final class NamedString {

        private final String name;
        private final String content;

        public NamedString(String name, String content) {
            this.name = name;
            this.content = content;
        }

            public static NamedString of(String name, String content) {
                return new NamedString(name, content);
            }

        public String name() {return name;}

        public String content() {return content;}

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NamedString) obj;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, content);
        }

        @Override
        public String toString() {
            return "NamedString[" +
                    "name=" + name + ", " +
                    "content=" + content + ']';
        }


        }

    public static final Set<String> EXTENSIONS_GRAPHML = Set.of(".graphml.xml", ".graphml");
    public static final Set<String> EXTENSIONS_CJ_JSON = Set.of(".cj.json", ".cj");
    static final List<NamedString> jsonInputs = List.of( //
            NamedString.of("number", "{\"foo\":42}"),//
            NamedString.of("string", "{\"foo\":\"bar\"}"),//
            NamedString.of("boolean-true", "{\"foo\":true}"),//
            NamedString.of("boolean-false", "{\"foo\":false}"),//
            NamedString.of("null", "{\"foo\":null}"),//
            NamedString.of("array-of-numbers", "{\"foo\":[1,2,3]}"),//
            NamedString.of("nested-object", "{\"foo\":{\"bar\":42}}"),//
            NamedString.of("empty-array", "[]"), //
            NamedString.of("empty-object", "{}"),//
            NamedString.of("mixed-array", "[1,\"two\",true,null,{},[]]"), //
            NamedString.of("two-properties", "{\"foo\":42,\"bar\":\"baz\"}"));
    /** Testing JSON 5 extensions */
    static final List<NamedString> inputsJson5 = List.of(
            // == Objects
            // test: Object keys may be an ECMAScript 5.1 IdentifierName.
            NamedString.of("object-identifier-name", "{foo:42}"),
            // test: Objects may have a single trailing comma.
            NamedString.of("object-trailing-comma", "{\"foo\":42,}"),
            // == Arrays
            // test: Arrays may have a single trailing comma.
            NamedString.of("array-trailing-comma", "[1,2,3,]"),
            // == Strings
            // test: Strings may be single quoted.
            NamedString.of("single-quoted-string", "{'foo':'bar'}"),
            // test: Strings may span multiple lines by escaping new line characters.
            NamedString.of("multiline-string", "{\"foo\":\"line1\\\nline2\"}"),
            // test: Strings may include character escapes.
            NamedString.of("escaped-chars", "{\"foo\":\"\\u0041\\t\\r\\n\"}"),
            // == Numbers
            // test: Numbers may be hexadecimal.
            // TODO not yet suported:
            // inputSource("hex-number", "{\"foo\":0xFF}"),
            // test: Numbers may have a leading or trailing decimal point.
            NamedString.of("decimal-point", "{\"foo\":.5,\"bar\":5.}"),
            // test: Numbers may be IEEE 754 positive infinity, negative infinity, and NaN.
            NamedString.of("special-numbers", "{\"foo\":Infinity,\"bar\":-Infinity,\"baz\":NaN}"),
            // test: Numbers may begin with an explicit plus sign.
            NamedString.of("plus-sign", "{\"foo\":+42}"),
            // ==  Comments
            // test: Single and multi-line comments are allowed.
            NamedString.of("comments", "{\"foo\":42} // comment\n/* multi\nline */"),
            // == White Space
            // test: Additional white space characters are allowed.
            NamedString.of("whitespace", "{\t\"foo\"\t:\t42\t}\n"));

    public static Stream<TestResource> cjResourcesCanonical() {
        return resources("json/cj_7_0_0", EXTENSIONS_CJ_JSON);
    }

    /** Only extended */
    public static Stream<TestResource> cjResourcesExtended() {
        return resources("json/cj_7_0_0", EXTENSIONS_CJ_JSON);
    }

    /**
     * Use the classpath resource mechanism to list ALL resources within packages 'xml','json','json5' on the current
     * classpath. Resulting paths have the syntax 'com/example/filename.ext'.
     */
    private static final String[] SCAN_PACKAGES = {"json", "xml", "json5", "text"};

    /**
     * All resources from the in-repo classpath, plus — if configured — the optional
     * {@link TestFileUtil#externalRoot() external graph-test-data root}. External resources are
     * yielded first, so on a path collision the external (migrated) copy wins.
     */
    private static Stream<Resource> scanResources() {
        Stream<Resource> classpath = new ClassGraph()
                .acceptPackages(SCAN_PACKAGES)
                .scan().getAllResources().stream();
        File external = TestFileUtil.externalRoot();
        if (external == null) {
            return classpath;
        }
        Stream<Resource> externalResources = new ClassGraph()
                .overrideClasspath(external.getAbsolutePath())
                .acceptPackages(SCAN_PACKAGES)
                .scan().getAllResources().stream();
        return Stream.concat(externalResources, classpath);
    }

    public static Stream<TestResource> getAllTestResources() {
        Set<String> seenPaths = new HashSet<>();
        return scanResources() //
                .filter(res -> !res.getPath().endsWith(".class")) //
                .filter(res -> seenPaths.add(res.getPath())) // dedup by path; external (first) wins
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

    public static Stream<Arguments> jsonInputSources() {
        return jsonInputs.stream().map(is -> Arguments.of(is.name(), is.content));
    }

    public static Stream<TestResource> jsonResources() {
        return resources("json", Set.of(".json"));
    }

    public static TestResource resourceByPath(String path) {
        return resources(path, Collections.emptySet()).findFirst().orElseThrow();
    }

    /**
     * @param allowedExtensions if empty, allow all. Syntax: '.json' (include dot, no stars)
     */
    public static Stream<TestResource> resources(String resourceRootPath, Set<String> allowedExtensions) {
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
