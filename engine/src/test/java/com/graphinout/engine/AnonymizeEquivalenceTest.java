package com.graphinout.engine;

import com.graphinout.base.cj.anonymize.AnonymizingCjStream;
import com.graphinout.base.cj.anonymize.CjDocumentAnonymizer;
import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.FileSingleInputSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies the streaming {@link AnonymizingCjStream} produces the same anonymization as the whole-document
 * {@link CjDocumentAnonymizer} for every file in the external graph-test-data corpus. Both are driven from the same
 * source document, so any difference is anonymizer logic (not reader stream order).
 */
class AnonymizeEquivalenceTest {

    private static final GioEngineCore CORE = new GioEngineCore();
    private static final GioInputAnalyzer ANALYZER = new GioInputAnalyzer(CORE.readers());

    @Test
    void streamingMatchesDocumentAnonymizerOnAllFiles() throws IOException {
        Path root = findTestDataRoot();
        assumeTrue(root != null, "graph-test-data repo not found next to graphinout — skipping");

        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(Files::isRegularFile).filter(AnonymizeEquivalenceTest::isDataFile).sorted().toList();
        }

        int compared = 0, match = 0, skipped = 0;
        List<String> mismatches = new ArrayList<>();
        for (Path file : files) {
            ICjDocument source = readSource(file);
            if (source == null) {
                skipped++;
                continue;
            }
            String viaDocument, viaStream;
            try {
                viaDocument = CjDocuments.toJsonString(CjDocumentAnonymizer.anonymize(source));

                CjWriter2CjDocumentWriter capture = new CjWriter2CjDocumentWriter();
                AnonymizingCjStream anon = new AnonymizingCjStream(new CjStream2CjWriter(capture, false));
                CjDocument2CjStream.toCjStream(source, anon);
                viaStream = CjDocuments.toJsonString(capture.resultDoc());
            } catch (Exception e) {
                skipped++;
                continue;
            }
            compared++;
            if (viaDocument.equals(viaStream)) {
                match++;
            } else if (mismatches.size() < 40) {
                mismatches.add("  " + root.relativize(file));
            }
        }

        System.out.printf("%nAnonymizer equivalence over graph-test-data:%n  files=%d compared=%d match=%d mismatch=%d skipped=%d%n",
                files.size(), compared, match, compared - match, skipped);
        if (!mismatches.isEmpty()) {
            System.out.println("  mismatches:");
            mismatches.forEach(System.out::println);
        }

        assertThat(compared).isGreaterThan(0);
        assertThat(match).isEqualTo(compared);
    }

    /** Detect the best reader for the file and read it into a CJ document; null if it cannot be read. */
    private static @Nullable ICjDocument readSource(Path file) {
        String formatId;
        try (FileSingleInputSource in = new FileSingleInputSource(file.toFile())) {
            GioInputAnalysis a = ANALYZER.analyze(in);
            if (a.best().isEmpty()) {
                return null;
            }
            formatId = a.best().get().format().id();
        } catch (Exception e) {
            return null;
        }
        GioReader reader = CORE.readers().stream()
                .filter(r -> r.fileFormat().id().equals(formatId)).findFirst().orElse(null);
        if (reader == null) {
            return null;
        }
        try (FileSingleInputSource in = new FileSingleInputSource(file.toFile())) {
            reader.setContentErrorHandler(e -> { });
            CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
            reader.read(in, new CjStream2CjWriter(w, true));
            return w.resultDoc();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isDataFile(Path p) {
        String lower = p.getFileName().toString().toLowerCase();
        if (lower.startsWith(".") || lower.endsWith(".ddot") || lower.endsWith(".adoc")
                || lower.endsWith(".md") || lower.endsWith(".iml") || lower.contains("--invalid")) {
            return false;
        }
        String path = p.toString();
        return !path.contains("/.idea/") && !path.contains("/.git/");
    }

    private static @Nullable Path findTestDataRoot() {
        for (Path p = Path.of("").toAbsolutePath(); p != null; p = p.getParent()) {
            Path candidate = p.resolve("graph-test-data");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
