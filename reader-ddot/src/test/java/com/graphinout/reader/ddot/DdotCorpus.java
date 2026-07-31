package com.graphinout.reader.ddot;

import org.junit.jupiter.api.Assumptions;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Resolves the cross-implementation ddot.it golden corpus ({@code ddot.it/test-data/cases}, 35 cases) for
 * the tests in this package.
 *
 * <p><strong>Absent is a skip; present-but-wrong is a failure.</strong> The corpus lives in a sibling
 * repository, so it can legitimately be missing — then every caller skips, after a stderr banner naming the
 * path it looked at and the {@code -D} override, so the skip cannot be mistaken for a pass. But a corpus
 * that is present and truncated, or a case missing a file, is a bug in the checkout, not a configuration
 * choice, and fails loudly. The distinction matters: a test that quietly evaporates when its data is
 * missing reports green forever and nobody notices it stopped asserting anything.
 */
final class DdotCorpus {

    /** Override for a corpus checked out somewhere other than as a sibling of the graphinout checkout. */
    static final String DIR_PROP = "ddot.corpus.dir";

    /**
     * Sibling checkouts of https://github.com/calpano/ddot.it, relative to this module's working directory
     * (surefire runs in {@code reader-ddot/}), covering a sibling-of-the-module and a sibling-of-the-repo
     * layout.
     */
    private static final List<Path> DEFAULTS = List.of(
            Path.of("..", "..", "ddot.it", "test-data", "cases"),
            Path.of("..", "ddot.it", "test-data", "cases"));

    /** The corpus has 35 cases; anything materially smaller means we are asserting against a stub. */
    static final int MIN_CASES = 30;

    private DdotCorpus() {}

    /** All case directories, sorted. Skips loudly when absent; fails when truncated. */
    static List<Path> caseDirs() throws IOException {
        Path dir = requireCasesDir();
        List<Path> caseDirs;
        try (Stream<Path> s = Files.list(dir)) {
            caseDirs = s.filter(Files::isDirectory).sorted(Comparator.naturalOrder()).toList();
        }
        if (caseDirs.size() < MIN_CASES) {
            fail("The ddot.it corpus at " + dir.toAbsolutePath().normalize() + " has only " + caseDirs.size()
                    + " case(s); at least " + MIN_CASES + " are expected (the corpus has 35). A truncated"
                    + " corpus would make conformance tests pass vacuously, so this is a failure, not a skip.");
        }
        return caseDirs;
    }

    /** The {@code input.ddot} of one named case, e.g. {@code 03-inline-meta}. */
    static String input(String caseName) throws IOException {
        return Files.readString(requireFile(requireCasesDir().resolve(caseName), "input.ddot"));
    }

    /** A file inside a case directory; a missing one fails rather than silently reducing coverage. */
    static Path requireFile(Path caseDir, String name) {
        Path p = caseDir.resolve(name);
        if (!Files.isRegularFile(p)) {
            fail("Corpus case " + caseDir.getFileName() + " is missing " + name + " (" + p + ")."
                    + " An incomplete case would be silently skipped, so this is a failure.");
        }
        return p;
    }

    static String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path requireCasesDir() {
        Path dir = resolveCasesDir();
        if (dir == null || !Files.isDirectory(dir)) {
            String looked = dir != null ? dir.toAbsolutePath().normalize().toString()
                    : DEFAULTS.stream().map(p -> p.toAbsolutePath().normalize().toString())
                            .reduce((a, b) -> a + "  |  " + b).orElse("?");
            System.err.println();
            System.err.println("==================================================================");
            System.err.println("  SKIPPING ddot.it CORPUS TESTS (reader-ddot)");
            System.err.println("  looked for : " + looked);
            System.err.println("  fix        : clone https://github.com/calpano/ddot.it as a sibling");
            System.err.println("               of the graphinout checkout, or run with");
            System.err.println("               -D" + DIR_PROP + "=/abs/path/to/ddot.it/test-data/cases");
            System.err.println("  NOTE       : DDotReader/DDotOutput are NOT being checked against");
            System.err.println("               the 35-case shared corpus in this run.");
            System.err.println("==================================================================");
            System.err.println();
            Assumptions.abort("ddot.it golden corpus not found — see the banner above. Override with -D"
                    + DIR_PROP + "=<abs path>.");
        }
        return dir;
    }

    /**
     * {@code -Dddot.corpus.dir} if set (pointing either at {@code cases/} itself or at the {@code test-data/}
     * directory containing it), else the first existing sibling checkout.
     */
    private static @Nullable Path resolveCasesDir() {
        String configured = System.getProperty(DIR_PROP);
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured.trim());
            Path nested = p.resolve("cases");
            return Files.isDirectory(nested) ? nested : p;
        }
        return DEFAULTS.stream().filter(Files::isDirectory).findFirst().orElse(null);
    }
}
