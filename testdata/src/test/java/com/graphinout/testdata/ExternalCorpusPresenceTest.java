package com.graphinout.testdata;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Fails the build when the external {@code graph-test-data} corpus is missing, unless its absence is
 * explicitly waived.
 *
 * <p><strong>Why this exists.</strong> Most of the suite is driven by that corpus through
 * {@link TestFileProvider}, and when it is absent the parameterized tests simply generate fewer cases.
 * Nothing fails, nothing is marked skipped, and the build reports success. Measured on this repo:
 *
 * <pre>
 *   mvn clean test                                        -&gt; 4167 tests, BUILD SUCCESS
 *   mvn clean test -Dgraph.test.data.dir=/nonexistent     -&gt; 1887 tests, BUILD SUCCESS
 * </pre>
 *
 * <p>That is 2280 tests — 55% of the suite — disappearing silently, taking with them the DOT/Graphviz
 * conformance sweep, the Neo4j round-trips and the CJ corpus tests. A green pipeline in that state is not
 * evidence the code works, which makes every other guarantee in this suite conditional on something no
 * output ever mentions.
 *
 * <p><strong>The trap this is really guarding.</strong>
 * {@code TestFileUtil.resolveExternalRoot()} returns {@code null} both when nothing is configured AND when
 * {@code -Dgraph.test.data.dir} points at a directory that does not exist. A typo in a CI config is
 * therefore indistinguishable from a deliberate opt-out — and silently costs half the suite. This test
 * separates the two cases and names the misconfigured path.
 *
 * <p>Auto-discovery is also relative to the working directory ({@code ../graph-test-data},
 * {@code ../../graph-test-data}), so it fails from a nested build directory such as a git worktree even
 * when the corpus is checked out normally.
 *
 * <p><strong>Waiving it.</strong> Run with {@code -Dgraph.test.data.optional=true} to build without the
 * corpus on purpose. That downgrades this to a loud skip: still visible in the report, but not a failure.
 * The point is not to force the corpus on everyone — it is to make its absence impossible to miss.
 */
class ExternalCorpusPresenceTest {

    /** Set to {@code true} to build deliberately without the corpus. */
    static final String OPT_OUT_PROPERTY = "graph.test.data.optional";

    /**
     * The corpus holds ~1000 files. A checkout with materially fewer is truncated (a failed clone, an
     * unfinished LFS fetch, a wrong directory) and would quietly shrink the suite just like an absent one.
     */
    static final int MIN_FILES = 300;

    @Test
    void externalCorpusIsPresentOrExplicitlyWaived() throws IOException {
        File root = TestFileUtil.externalRoot();
        boolean waived = Boolean.getBoolean(OPT_OUT_PROPERTY);

        if (root == null) {
            String configured = System.getProperty(TestFileUtil.EXTERNAL_ROOT_PROPERTY);
            if (configured == null || configured.isBlank()) {
                configured = System.getenv(TestFileUtil.EXTERNAL_ROOT_ENV);
            }
            boolean misconfigured = configured != null && !configured.isBlank();
            banner(misconfigured
                            ? "CONFIGURED PATH DOES NOT EXIST: " + configured
                            : "not configured, and no sibling ../graph-test-data or ../../graph-test-data",
                    waived);
            if (waived) {
                Assumptions.abort("graph-test-data corpus absent, waived via -D" + OPT_OUT_PROPERTY
                        + "=true — see the banner above. Roughly half the test suite did NOT run.");
            }
            fail("The external graph-test-data corpus is not available"
                    + (misconfigured ? " (configured path does not exist: " + configured + ")" : "")
                    + ". Without it roughly half the suite silently does not run, so the build is failed"
                    + " here rather than reporting a green run that proves little. See the banner above;"
                    + " waive deliberately with -D" + OPT_OUT_PROPERTY + "=true.");
        }

        long files;
        try (Stream<Path> s = Files.walk(root.toPath())) {
            files = s.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().contains(File.separator + ".git" + File.separator))
                    .count();
        }
        if (files < MIN_FILES) {
            fail("The graph-test-data corpus at " + root.getAbsolutePath() + " holds only " + files
                    + " file(s); at least " + MIN_FILES + " are expected (a full checkout has ~1000)."
                    + " A truncated corpus shrinks the suite exactly like a missing one, so this is a"
                    + " failure rather than a quietly smaller test run.");
        }
    }

    private static void banner(String reason, boolean waived) {
        System.err.println();
        System.err.println("==================================================================");
        System.err.println("  graph-test-data CORPUS NOT FOUND — " + (waived ? "WAIVED" : "FAILING THE BUILD"));
        System.err.println("  reason     : " + reason);
        System.err.println("  working dir: " + new File(".").getAbsolutePath());
        System.err.println("  impact     : ~55% of the test suite does not run, WITHOUT any test");
        System.err.println("               failing or being marked skipped (4167 -> 1887 tests).");
        System.err.println("  fix        : clone https://github.com/Calpano/graph-test-data as a");
        System.err.println("               sibling of this repo, or set");
        System.err.println("               -D" + TestFileUtil.EXTERNAL_ROOT_PROPERTY + "=/abs/path/to/graph-test-data");
        System.err.println("               (or env " + TestFileUtil.EXTERNAL_ROOT_ENV + ")");
        System.err.println("  waive      : -D" + OPT_OUT_PROPERTY + "=true");
        System.err.println("==================================================================");
        System.err.println();
    }
}
