package com.graphinout.engine.doc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Keeps the Java snippets in the developer docs compiled and tested. GitHub does not process AsciiDoc
 * {@code include::} directives, so the docs carry copies of the code; this test fails when a copy no longer matches
 * its {@code // tag::name[]} ... {@code // end::name[]} region in the source.
 *
 * <p>To fix a failure, copy the (dedented) tagged region from the source file into the doc's
 * {@code [source,java]} block.
 */
class DocSnippetsTest {

    private static final String USAGE_EXAMPLES = "engine/src/test/java/com/graphinout/engine/doc/ApiUsageExamplesTest.java";
    private static final String EXAMPLE_READER = "reader-example/src/main/java/com/graphinout/reader/example/ExampleReader.java";
    private static final String TGF_WRITER = "reader-tgf/src/main/java/com/graphinout/reader/tgf/TgfWriter.java";
    private static final String EXAMPLE_SERVICE = "reader-example/src/main/java/com/graphinout/reader/example/ExampleService.java";

    static Stream<Arguments> snippets() {
        return Stream.of(
                Arguments.of("README.adoc", USAGE_EXAMPLES, "readme"),
                Arguments.of("doc/usage.adoc", USAGE_EXAMPLES, "convert"),
                Arguments.of("doc/usage.adoc", USAGE_EXAMPLES, "stream"),
                Arguments.of("doc/usage.adoc", USAGE_EXAMPLES, "errors"),
                Arguments.of("doc/usage.adoc", USAGE_EXAMPLES, "detect"),
                Arguments.of("doc/usage.adoc", USAGE_EXAMPLES, "formats"),
                Arguments.of("doc/writing-a-converter.adoc", EXAMPLE_SERVICE, "service"),
                Arguments.of("doc/writing-a-converter.adoc", EXAMPLE_READER, "reader"),
                Arguments.of("doc/writing-a-converter.adoc", TGF_WRITER, "writer"));
    }

    /** Dedent by the smallest indentation of any non-blank line. */
    static String dedent(List<String> lines) {
        int indent = lines.stream().filter(l -> !l.isBlank())
                .mapToInt(l -> l.length() - l.stripLeading().length()).min().orElse(0);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line.isBlank() ? "" : line.substring(indent)).append('\n');
        }
        return sb.toString();
    }

    static String extractTag(String source, String tag) {
        List<String> lines = Arrays.asList(source.split("\n", -1));
        int start = -1, end = -1;
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (t.equals("// tag::" + tag + "[]")) start = i;
            if (t.equals("// end::" + tag + "[]")) end = i;
        }
        if (start < 0 || end <= start) throw new IllegalStateException("No tag '" + tag + "' in source");
        return dedent(lines.subList(start + 1, end));
    }

    /** Maven runs tests in the module dir, IDEs often in the repo root; find the root either way. */
    static Path repoRoot() {
        Path p = Path.of("").toAbsolutePath();
        while (p != null && !Files.isDirectory(p.resolve("doc"))) p = p.getParent();
        if (p == null) throw new IllegalStateException("Cannot find repo root (a dir containing doc/)");
        return p;
    }

    static String read(Path root, String rel) throws IOException {
        return Files.readString(root.resolve(rel)).replace("\r\n", "\n");
    }

    @ParameterizedTest(name = "{0} contains {2} from {1}")
    @MethodSource("snippets")
    void docContainsSnippet(String doc, String source, String tag) throws IOException {
        Path root = repoRoot();
        String snippet = extractTag(read(root, source), tag);
        assertWithMessage("%s is out of date: copy tag '%s' from %s into it", doc, tag, source)
                .that(read(root, doc)).contains(snippet);
    }

}
