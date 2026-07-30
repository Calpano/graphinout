package com.graphinout.reader.ddot;

import com.calpano.ddot.it.event.DdotEventExporter;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Conformance of this reader against the cross-implementation ddot.it golden corpus
 * ({@code ddot.it/test-data/cases}, 35 cases) — the contract every ddot.it implementation shares.
 *
 * <p>Three things are asserted per case:
 * <ol>
 *   <li><strong>the event stream</strong> the reader parses with is byte-identical to
 *       {@code expected.events.jsonl}. Since {@link DDotReader} no longer implements the grammar but folds
 *       {@link DdotEventExporter}'s output, this pins the exact parser the reader consumes — the corpus
 *       cases the old hand-rolled parser got wrong (20-object-dotdot, 23-not-a-triple, 31-off-in-comment,
 *       22/28/33 {@code ;;}, every {@code !!block} case) are all in here;</li>
 *   <li><strong>no ContentError above {@code Warn}</strong> — every case must import cleanly;</li>
 *   <li><strong>every event becomes exactly one CJ fact</strong> — an edge, a node type, a node label, a
 *       node data/{@code rdf:data} property, a document property or an {@code @context} entry. This is the
 *       assertion that actually binds the CJ importer to the corpus: it fails if the vocabulary fold drops
 *       an event (an edge that never materialises) or invents one, for all 35 cases at once. It does not
 *       re-derive <em>which</em> fact each event should become — that is what the hand-written unit tests
 *       in this package are for — it only holds the fold total-and-injective.</li>
 * </ol>
 *
 * <p><strong>This test refuses to pass vacuously.</strong> The corpus lives in a sibling repository, so it
 * may legitimately be absent — then the test skips and prints a loud banner naming the path and the
 * {@code -D} override. A corpus that is present but <em>truncated</em> is a bug, not a configuration:
 * fewer than {@value #MIN_CASES} cases, or a case missing a file, is a FAILURE.
 */
class DDotCorpusConformanceTest {

    /** Override for a corpus checked out somewhere other than as a sibling of the graphinout checkout. */
    private static final String DIR_PROP = "ddot.corpus.dir";

    /**
     * Sibling checkouts of https://github.com/calpano/ddot.it, relative to this module's working directory
     * (surefire runs in {@code reader-ddot/}), covering both a sibling-of-the-module and a
     * sibling-of-the-repo layout.
     */
    private static final List<Path> DEFAULT_CASES = List.of(
            Path.of("..", "..", "ddot.it", "test-data", "cases"),
            Path.of("..", "ddot.it", "test-data", "cases"));

    /** The corpus has 35 cases; anything materially smaller means we are asserting against a stub. */
    static final int MIN_CASES = 30;

    private static final String EVENT_KIND = "ddot";
    private static final String EVENT_SOURCE = "input.ddot";

    @Test
    void readerParsesWithTheCanonicalEventStream() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path caseDir : cases()) {
            String input = Files.readString(requireFile(caseDir, "input.ddot"));
            String expected = Files.readString(requireFile(caseDir, "expected.events.jsonl"));
            String actual = DdotEventExporter.toJsonl(DdotEventExporter.parse(input, EVENT_KIND, EVENT_SOURCE));
            if (!expected.stripTrailing().equals(actual.stripTrailing())) {
                failures.add(caseDir.getFileName().toString());
                System.err.println("[corpus] EVENT MISMATCH " + caseDir.getFileName());
                System.err.println("---- expected ----\n" + expected);
                System.err.println("---- actual   ----\n" + actual);
            }
        }
        if (!failures.isEmpty()) {
            fail(failures.size() + " case(s) diverged from expected.events.jsonl: " + String.join(", ", failures)
                    + ". The event stream is the shared cross-implementation contract; fix ddot-core"
                    + " (com.calpano.ddot.it:ddot-core), never the corpus.");
        }
    }

    @Test
    void everyCaseImportsWithoutErrors() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path caseDir : cases()) {
            List<ContentError> errors = new ArrayList<>();
            read(Files.readString(requireFile(caseDir, "input.ddot")), caseDir.getFileName().toString(), errors);
            List<ContentError> fatal = errors.stream().filter(ContentError::isError).toList();
            if (!fatal.isEmpty()) failures.add(caseDir.getFileName() + " " + fatal);
        }
        if (!failures.isEmpty()) fail("Corpus case(s) reported errors above Warn: " + String.join("; ", failures));
    }

    @Test
    void everyEventBecomesExactlyOneCjFact() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path caseDir : cases()) {
            String input = Files.readString(requireFile(caseDir, "input.ddot"));
            int expected = Files.readAllLines(requireFile(caseDir, "expected.events.jsonl")).stream()
                    .filter(l -> !l.isBlank()).toList().size();
            ICjDocument doc = read(input, caseDir.getFileName().toString(), new ArrayList<>());
            int actual = cjFacts(doc);
            if (expected != actual) {
                failures.add(caseDir.getFileName() + ": " + expected + " event(s) but " + actual + " CJ fact(s)");
            }
        }
        if (!failures.isEmpty()) {
            fail("The vocabulary fold in DDotReader lost or invented facts: " + String.join("; ", failures)
                    + ". Every ddot.it event must land in the CJ document as exactly one of: edge, node type,"
                    + " node label, node data property, document property, @context entry.");
        }
    }

    /**
     * Every case must survive {@code ddot -> CJ -> ddot -> CJ} unchanged. This is the WRITER's half of the
     * contract, and it is the half that rotted: the reader became spec-conformant first, so it started
     * accepting constructs {@link DDotOutput} could not spell — a multi-line subject, a multi-line metadata
     * value, a value containing {@code ;;} — and each of those was written back in a form that read as
     * something else (or, for {@code ;;}, as nothing at all: the line failed to derive and the triple was
     * silently lost). Asserting only the read direction, as the tests above do, cannot see any of that.
     *
     * <p>A case that yields no facts at all ({@code 23-not-a-triple} is pure prose) writes back as the
     * empty string, which the reader answers with a bare document and no graph rather than an empty one.
     * That asymmetry loses nothing, so it is asserted as "still no facts" instead of by document equality.
     */
    @Test
    void everyCaseRoundTripsThroughTheWriter() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path caseDir : cases()) {
            String name = caseDir.getFileName().toString();
            ICjDocument cj1 = read(Files.readString(requireFile(caseDir, "input.ddot")), name, new ArrayList<>());
            String ddot2 = new DDotOutput(cj1).toDDot();
            ICjDocument cj2 = read(ddot2, name + ".roundtrip", new ArrayList<>());
            if (cjFacts(cj1) == 0) {
                if (cjFacts(cj2) != 0) failures.add(name + ": no facts became " + cjFacts(cj2));
                continue;
            }
            String json1 = CjDocuments.toJsonString(cj1);
            String json2 = CjDocuments.toJsonString(cj2);
            if (!json1.equals(json2)) {
                failures.add(name);
                System.err.println("[corpus] ROUND-TRIP MISMATCH " + name);
                System.err.println("---- ddot written from CJ ----\n" + ddot2);
                System.err.println("---- CJ 1 ----\n" + json1);
                System.err.println("---- CJ 2 ----\n" + json2);
            }
        }
        if (!failures.isEmpty()) {
            fail(failures.size() + " case(s) did not survive ddot -> CJ -> ddot: " + String.join(", ", failures)
                    + ". DDotOutput/DDotDoc must spell every construct the reader accepts; see the"
                    + " block/`;;` rules in DDotDoc.appendMeta.");
        }
    }

    // --- reading & counting --------------------------------------------------

    private static ICjDocument read(String ddot, String name, List<ContentError> errors) throws IOException {
        DDotReader reader = new DDotReader();
        reader.setContentErrorHandler(errors::add);
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of(name + "/input.ddot", ddot), new CjStream2CjWriter(cj2document, true));
        return cj2document.resultDoc();
    }

    /** Every graph fact the reader can produce from one event, counted once. */
    private static int cjFacts(ICjDocument doc) {
        int facts = 0;
        Map<String, String> context = doc.context();
        if (context != null) facts += context.size();
        facts += jsonEntries(doc.data().jsonValue());              // ddot.it/this document properties
        for (ICjGraph graph : doc.graphs().toList()) {
            facts += (int) graph.edges().count();
            for (ICjNode node : graph.nodes().toList()) {
                facts += (int) node.types().count();               // `has type` and its aliases
                facts += node.labelEntries().size();               // `label` command
                IJsonValue data = node.data().jsonValue();
                if (data != null && data.isObject()) {
                    for (String key : data.asObject().keys()) {
                        IJsonValue value = data.asObject().get(key);
                        // `rdf:data` is a container of literal properties: count its entries, not itself
                        facts += DDotOutput.RDF_DATA_KEY.equals(key) ? jsonEntries(value) : countValues(value);
                    }
                }
            }
        }
        return facts;
    }

    /** Number of scalar entries in a JSON object, counting a repeated key's array elements individually. */
    private static int jsonEntries(@org.jspecify.annotations.Nullable IJsonValue object) {
        if (object == null || !object.isObject()) return 0;
        int n = 0;
        for (String key : object.asObject().keys()) n += countValues(object.asObject().get(key));
        return n;
    }

    private static int countValues(@org.jspecify.annotations.Nullable IJsonValue value) {
        if (value == null) return 0;
        return value.isArray() ? value.asArray().size() : 1;
    }

    // --- corpus resolution ---------------------------------------------------

    /**
     * The case directories, or a loud skip when the corpus is not checked out. Never returns an empty or
     * suspiciously short list: that fails instead, so a missing corpus can never masquerade as a pass.
     */
    private static List<Path> cases() throws IOException {
        Path dir = resolveCasesDir();
        if (dir == null || !Files.isDirectory(dir)) {
            String looked = dir != null ? dir.toAbsolutePath().normalize().toString()
                    : DEFAULT_CASES.stream().map(p -> p.toAbsolutePath().normalize().toString())
                            .reduce((a, b) -> a + " , " + b).orElse("?");
            System.err.println();
            System.err.println("==================================================================");
            System.err.println("  SKIPPING ddot.it CORPUS CONFORMANCE TESTS (reader-ddot)");
            System.err.println("  looked for : " + looked);
            System.err.println("  fix        : clone https://github.com/calpano/ddot.it as a sibling");
            System.err.println("               of the graphinout checkout, or run with");
            System.err.println("               -D" + DIR_PROP + "=/abs/path/to/ddot.it/test-data/cases");
            System.err.println("  NOTE       : DDotReader is NOT being checked against the 35-case");
            System.err.println("               shared corpus in this run.");
            System.err.println("==================================================================");
            System.err.println();
            Assumptions.abort("ddot.it golden corpus not found — see the banner above. Override with -D"
                    + DIR_PROP + "=<abs path>.");
        }
        List<Path> caseDirs;
        try (Stream<Path> s = Files.list(dir)) {
            caseDirs = s.filter(Files::isDirectory).sorted(Comparator.naturalOrder()).toList();
        }
        if (caseDirs.size() < MIN_CASES) {
            fail("The ddot.it corpus at " + dir.toAbsolutePath().normalize() + " has only " + caseDirs.size()
                    + " case(s); at least " + MIN_CASES + " are expected (the corpus has 35). A truncated"
                    + " corpus would make this conformance test pass vacuously, so this is a failure, not a skip.");
        }
        return caseDirs;
    }

    /**
     * {@code -Dddot.corpus.dir} if set (pointing either at {@code cases/} itself or at the {@code test-data/}
     * directory containing it), else the first existing sibling checkout. Relative paths resolve against the
     * working directory, which surefire sets to this module's directory.
     */
    private static @org.jspecify.annotations.Nullable Path resolveCasesDir() {
        String configured = System.getProperty(DIR_PROP);
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured.trim());
            Path nested = p.resolve("cases");
            return Files.isDirectory(nested) ? nested : p;
        }
        return DEFAULT_CASES.stream().filter(Files::isDirectory).findFirst().orElse(null);
    }

    private static Path requireFile(Path caseDir, String name) {
        Path p = caseDir.resolve(name);
        if (!Files.isRegularFile(p)) {
            fail("Corpus case " + caseDir.getFileName() + " is missing " + name + " (" + p + ")."
                    + " An incomplete case would be silently skipped, so this is a failure.");
        }
        return p;
    }
}
