package com.graphinout.engine;

import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.input.FileSingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end detection accuracy over the external <a href="https://github.com/Calpano/graph-test-data">graph-test-data</a>
 * collection: feed every (valid) file to the detector and compare the best-ranked format to the file's declared truth.
 *
 * <p>Ground truth comes from the {@code .ddot} metadata ({@code ..format.. format:<id>} — sidecar
 * {@code <file>.ddot} or a per-directory {@code meta.ddot}), falling back to the path family (the repo files by
 * {@code <category>/<family>/…}). The expected id is normalised to its family (dropping a version suffix, e.g.
 * {@code connected-json-7.0.0 -> connected-json}). Intentionally-broken {@code --INVALID} files and families the
 * engine has no reader for are skipped.
 *
 * <p>The suite is skipped when the sibling {@code graph-test-data} repo is not checked out next to this one.
 */
class DetectGraphTestDataTest {

    private static final GioEngineCore CORE = new GioEngineCore();
    private static final GioInputAnalyzer ANALYZER = new GioInputAnalyzer(CORE.readers());
    private static final Set<String> READER_IDS =
            CORE.readers().stream().map(r -> r.fileFormat().id()).collect(Collectors.toSet());

    private static final Pattern FORMAT_TRIPLE = Pattern.compile("\\.\\.format\\.\\.\\s+format:(\\S+)");

    @Test
    void detectorMatchesDeclaredFormat() throws IOException {
        Path root = findTestDataRoot();
        assumeTrue(root != null, "graph-test-data repo not found next to graphinout — skipping");

        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(Files::isRegularFile).filter(DetectGraphTestDataTest::isDataFile).sorted().toList();
        }

        int pass = 0, fail = 0, skipNoExpected = 0, skipNoReader = 0;
        TreeMap<String, int[]> perFamily = new TreeMap<>(); // family -> {pass, total}
        List<String> mismatches = new ArrayList<>();

        for (Path file : files) {
            String expected = expectedFamily(file, root);
            if (expected == null) {
                skipNoExpected++;
                continue;
            }
            if (!READER_IDS.contains(expected)) {
                skipNoReader++;
                continue;
            }
            String detected = detect(file);
            int[] tally = perFamily.computeIfAbsent(expected, k -> new int[2]);
            tally[1]++;
            if (expected.equals(detected)) {
                pass++;
                tally[0]++;
            } else {
                fail++;
                if (mismatches.size() < 60) {
                    mismatches.add(String.format("  %-22s expected %-16s got %s", root.relativize(file), expected, detected));
                }
            }
        }

        int total = pass + fail;
        StringBuilder report = new StringBuilder("\nDetection over graph-test-data:\n");
        report.append(String.format("  files=%d  evaluated=%d  pass=%d  fail=%d  (skipped: no-format=%d, no-reader=%d)%n",
                files.size(), total, pass, fail, skipNoExpected, skipNoReader));
        report.append(String.format("  accuracy=%.1f%%%n", total == 0 ? 0.0 : 100.0 * pass / total));
        report.append("  per family (pass/total):\n");
        perFamily.forEach((fam, t) -> report.append(String.format("    %-18s %d/%d%n", fam, t[0], t[1])));
        if (!mismatches.isEmpty()) {
            report.append("  mismatches (first ").append(mismatches.size()).append("):\n");
            mismatches.forEach(m -> report.append(m).append('\n'));
        }
        System.out.println(report);

        assertThat(total).isGreaterThan(0);
        // Aggregate regression gate: detection picks the declared format as best-ranked candidate for the large
        // majority of files. The residual misses are mostly reader-capability gaps (the correct reader fails to parse,
        // leaving a permissive fallback as the only recovery) and genuinely degenerate/ambiguous files — see the
        // per-family report above. Tighten as readers improve.
        double accuracyPercent = 100.0 * pass / total;
        assertThat(accuracyPercent).isAtLeast(90.0);
    }

    // -- detection --------------------------------------------------------------------------------------------------

    private static String detect(Path file) {
        try (FileSingleInputSource in = new FileSingleInputSource(file.toFile())) {
            GioInputAnalysis a = ANALYZER.analyze(in);
            return a.best().map(c -> c.format().id()).orElse("<none>");
        } catch (Exception e) {
            return "<error:" + e.getClass().getSimpleName() + ">";
        }
    }

    // -- ground truth -----------------------------------------------------------------------------------------------

    private static String expectedFamily(Path file, Path root) {
        String raw = formatFromDdot(file);
        if (raw == null) {
            raw = familyFromPath(file, root);
        }
        return raw == null ? null : normalize(raw);
    }

    /** {@code ..format.. format:<id>} from the per-file sidecar, else the per-directory {@code meta.ddot}. */
    private static String formatFromDdot(Path file) {
        Path sidecar = file.resolveSibling(file.getFileName() + ".ddot");
        String fromSidecar = scanFormat(sidecar, null);
        if (fromSidecar != null) {
            return fromSidecar;
        }
        return scanFormat(file.resolveSibling("meta.ddot"), file.getFileName().toString());
    }

    /**
     * First {@code ..format.. format:<id>} object in {@code ddotFile}. When {@code subjectFilename} is given (a
     * per-directory meta.ddot), prefer a line whose subject is that filename, else fall back to a {@code ddot.it/this}
     * line.
     */
    private static String scanFormat(Path ddotFile, String subjectFilename) {
        if (!Files.isRegularFile(ddotFile)) {
            return null;
        }
        try {
            String fileScoped = null, thisScoped = null;
            for (String line : Files.readAllLines(ddotFile)) {
                Matcher m = FORMAT_TRIPLE.matcher(line);
                if (!m.find()) {
                    continue;
                }
                String fmt = m.group(1);
                if (subjectFilename == null) {
                    return fmt; // sidecar: single subject
                }
                if (line.startsWith(subjectFilename + " ")) {
                    fileScoped = fmt;
                } else if (line.startsWith("ddot.it/this ")) {
                    thisScoped = fmt;
                }
            }
            return fileScoped != null ? fileScoped : thisScoped;
        } catch (IOException e) {
            return null;
        }
    }

    /** {@code <category>/<family>/…} → family. */
    private static String familyFromPath(Path file, Path root) {
        Path rel = root.relativize(file);
        return rel.getNameCount() >= 2 ? rel.getName(1).toString() : null;
    }

    /** Drop a version suffix: {@code connected-json-7.0.0 -> connected-json}, {@code graphml-1.1 -> graphml}. */
    private static String normalize(String formatId) {
        return formatId.replaceFirst("-\\d.*$", "");
    }

    // -- file selection ---------------------------------------------------------------------------------------------

    private static boolean isDataFile(Path p) {
        String name = p.getFileName().toString();
        String lower = name.toLowerCase();
        if (name.startsWith(".")) {
            return false; // .DS_Store, dotfiles
        }
        if (lower.endsWith(".ddot") || lower.endsWith(".adoc") || lower.endsWith(".md") || lower.endsWith(".iml")) {
            return false; // metadata / docs / project files
        }
        if (lower.contains("--invalid")) {
            return false; // intentionally broken
        }
        String path = p.toString();
        return !path.contains("/.idea/") && !path.contains("/.git/");
    }

    // -- locate the sibling repo ------------------------------------------------------------------------------------

    private static Path findTestDataRoot() {
        for (Path p = Path.of("").toAbsolutePath(); p != null; p = p.getParent()) {
            Path candidate = p.resolve("graph-test-data");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
