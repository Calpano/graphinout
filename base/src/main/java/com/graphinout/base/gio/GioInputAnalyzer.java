package com.graphinout.base.gio;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.gio.GioInputAnalysis.Candidate;
import com.graphinout.base.gio.GioInputAnalysis.ConfidenceTier;
import com.graphinout.base.gio.GioInputAnalysis.ContentKind;
import com.graphinout.base.gio.GioInputAnalysis.Outcome;
import com.graphinout.base.gio.GioInputAnalysis.Signal;
import com.graphinout.base.gio.GioInputAnalysis.SignalKind;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Format detector: probes <em>every</em> {@link GioReader} against one input and ranks them by what each actually
 * recovers, producing a {@link GioInputAnalysis}. This is the engine behind a {@code detect} command / {@code /api/detect}
 * endpoint; it hosts the detection policy (scoring, tiering) that the result type deliberately does not.
 *
 * <p>Per reader the outcome is one of {@link Outcome}: a hard parse error or error-level {@link ContentError}
 * {@linkplain Outcome#ELIMINATED eliminates} it; a clean parse into a 0-node graph is {@linkplain Outcome#TRIVIAL
 * de-ranked} ("probably not this"); a parse into a real graph is {@linkplain Outcome#RECOVERED scored} by size and an
 * extension match. Large inputs are {@linkplain Outcome#SKIPPED skipped} and ranked on structural signals only.
 */
public class GioInputAnalyzer {

    private static final Logger log = getLogger(GioInputAnalyzer.class);

    /** Above this size, skip the per-reader parse and rank on cheap structural signals only. */
    public static final long LARGE_FILE_THRESHOLD_BYTES = 1_000_000L;

    /** A clear winner needs to beat the runner-up by at least this confidence gap to be {@link ConfidenceTier#CONFIDENT}. */
    private static final double CONFIDENT_MARGIN = 0.2;

    private final List<GioReader> readers;

    public GioInputAnalyzer(List<GioReader> readers) {
        this.readers = readers;
    }

    public GioInputAnalysis analyze(SingleInputSource input) throws IOException {
        String name = input.name();
        String content = input.getContentAsUtf8String();
        long sizeBytes = content.getBytes(StandardCharsets.UTF_8).length;
        ContentKind contentKind = sniffContentKind(content);
        boolean tooLarge = sizeBytes > LARGE_FILE_THRESHOLD_BYTES;
        // Does the filename carry an extension that some reader claims? If so, a reader that does NOT claim it is
        // probably wrong (e.g. a .nq file fed to the line-based adjlist reader).
        boolean knownExtension = readers.stream().anyMatch(r -> r.fileFormat().matches(name));

        List<Candidate> candidates = new ArrayList<>();
        for (GioReader reader : readers) {
            // For a large input, skip the expensive parse for unlikely readers, but still parse the MOST LIKELY
            // format(s) — the one(s) whose extension matches the filename — so big files are still detected.
            boolean parse = !tooLarge || reader.fileFormat().matches(name);
            candidates.add(parse
                    ? probe(reader, name, content, contentKind, knownExtension)
                    : skippedCandidate(reader, name, sizeBytes));
        }

        // best-first: by confidence, then extension match, then format id for determinism
        candidates.sort(Comparator
                .comparingDouble(Candidate::confidence).reversed()
                .thenComparing(c -> c.format().matches(name) ? 0 : 1)
                .thenComparing(c -> c.format().id()));

        return new AnalysisResult(name, OptionalLong.of(sizeBytes), contentKind, tooLarge, candidates,
                tierOf(candidates));
    }

    // -- per-reader probing -----------------------------------------------------------------------------------------

    private Candidate probe(GioReader reader, String name, String content, ContentKind inputKind,
                            boolean nameHasKnownExtension) {
        GioFileFormat format = reader.fileFormat();
        boolean extMatch = format.matches(name);
        List<ContentError> errors = new ArrayList<>();
        reader.setContentErrorHandler(errors::add);

        ICjDocument doc;
        try {
            doc = reader.readToCjDocument(SingleInputSource.of(name, content));
        } catch (Throwable t) {
            log.debug("Reader {} could not parse {}: {}", format.id(), name, t.toString());
            errors.add(ContentError.error(t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
            doc = null;
        }

        // IDENTITY is independent of parsing: a file's extension, content kind, filename and (above all) format-specific
        // vocabulary/signature identify WHAT format it is — even when no reader can parse it. A file that clearly looks
        // like GraphML IS GraphML (just possibly invalid); we detect what it contains, never silently re-label it.
        List<Signal> signals = new ArrayList<>();
        double extBonus = extMatch ? 0.15 : (nameHasKnownExtension ? -0.35 : 0.0);
        if (extMatch) {
            signals.add(Signal.of(SignalKind.EXTENSION, 0.3, "filename matches a " + format.label() + " extension"));
        } else if (nameHasKnownExtension) {
            signals.add(Signal.of(SignalKind.EXTENSION, -0.5, "filename extension belongs to a different format"));
        }
        double evidence = extBonus
                + contentKindBonus(inputKind, format, extMatch, signals)
                + filenameBonus(name, format, signals)
                + vocabularyBonus(content, format, signals)
                + signatureScore(content, format, signals);
        long errorCount = errors.stream().filter(e -> e.getLevel() == ContentError.ErrorLevel.Error).count();

        if (doc == null) {
            // Total parse failure is NOT proof the file is some OTHER format. If the identity markers say X, it IS X —
            // reported as an invalid/unparseable instance, ranked by identity alone (no recovery bonus).
            String why = errors.isEmpty() ? "" : ": " + errors.get(0).getMessage();
            signals.add(Signal.of(SignalKind.PARSE, -1.0, "could not be parsed" + why));
            double confidence = clamp(evidence);
            String explanation = confidence >= GioInputAnalysis.BEST_CONFIDENCE_FLOOR
                    ? "Looks like " + format.label() + ", but is invalid / could not be parsed" + why
                    : "Could not parse as " + format.label() + why;
            return new CandidateResult(format, Outcome.ELIMINATED, confidence, Optional.empty(), signals,
                    List.copyOf(errors), explanation);
        }

        CjAnalysis stats = CjAnalyzer.analyze(doc);
        // Validity errors don't disqualify a format that still produced a graph — they only lower confidence, so a
        // clean parse outranks a salvaged one.
        double errorPenalty = 0.0;
        if (errorCount > 0) {
            signals.add(Signal.of(SignalKind.PARSE, -0.3,
                    "parsed with " + errorCount + " error(s) — malformed but recognisable as " + format.label()));
            errorPenalty = 0.25;
        }

        // A reader that parses to an empty graph is de-ranked but NOT discarded: blank input, or a real format that
        // simply carries no graph, lands here. Its confidence rests on the evidence above, not on recovered size.
        if (content.isBlank() || (stats.nodeCount() == 0 && stats.edgeCount() == 0)) {
            signals.add(Signal.of(SignalKind.PARSE, -0.6, "parsed cleanly but the graph is empty"));
            double confidence = clamp(0.30 + evidence - errorPenalty);
            String why = confidence >= GioInputAnalysis.BEST_CONFIDENCE_FLOOR
                    ? "Parsed but empty — looks like " + format.label() + " with no graph content"
                    : "Parsed but graph is empty — probably not " + format.label();
            return new CandidateResult(format, Outcome.TRIVIAL, confidence, Optional.of(stats), signals,
                    List.copyOf(errors), why);
        }

        signals.add(Signal.of(SignalKind.PARSE, 1.0, "parsed into a non-empty graph ("
                + stats.nodeCount() + " nodes, " + stats.edgeCount() + " edges)"));
        if (!stats.features().isEmpty()) {
            signals.add(Signal.of(SignalKind.FEATURES, Math.min(0.3, 0.05 * stats.features().size()),
                    "recovered features: " + String.join(", ", stats.featureSlugs())));
        }
        // Structural plausibility: a misparse leaks stray syntax / orphans into the graph. Penalises an implausible
        // graph (a clean one is neutral), so the format that recovers a SANE graph wins over one that scavenges junk.
        double plausibility = graphPlausibility(doc);
        double implausibilityPenalty = (1.0 - plausibility) * 0.25;
        if (implausibilityPenalty > 0.01) {
            signals.add(Signal.of(SignalKind.PLAUSIBILITY, -implausibilityPenalty, String.format(Locale.ROOT,
                    "graph plausibility %.0f%% — stray syntax / orphan nodes", plausibility * 100)));
        }
        // Recovery is binary: a non-empty parse adds a fixed bonus. We deliberately do NOT score by graph size — so a
        // greedy line-based reader cannot win by fabricating more nodes/edges than the true format recovers.
        double confidence = clamp(0.55 + evidence - errorPenalty - implausibilityPenalty);
        return new CandidateResult(format, Outcome.RECOVERED, confidence, Optional.of(stats), signals,
                List.copyOf(errors),
                "Parsed " + stats.nodeCount() + " nodes / " + stats.edgeCount() + " edges"
                        + (extMatch ? ", extension matches" : ""));
    }

    private Candidate skippedCandidate(GioReader reader, String name, long sizeBytes) {
        GioFileFormat format = reader.fileFormat();
        boolean extMatch = format.matches(name);
        List<Signal> signals = new ArrayList<>();
        signals.add(Signal.of(SignalKind.PARSE, 0.0, "not parsed (input " + sizeBytes + " bytes exceeds threshold)"));
        if (extMatch) {
            signals.add(Signal.of(SignalKind.EXTENSION, 0.5, "filename matches a " + format.label() + " extension"));
        }
        double confidence = extMatch ? 0.5 : 0.1;
        return new CandidateResult(format, Outcome.SKIPPED, confidence, Optional.empty(), signals, List.of(),
                "Skipped deep analysis (" + sizeBytes + " bytes > " + LARGE_FILE_THRESHOLD_BYTES + ")");
    }

    // -- scoring helpers --------------------------------------------------------------------------------------------

    /**
     * Structural cross-check: when the raw bytes are clearly JSON or XML, a reader of the matching kind is encouraged
     * and one of a different kind is strongly discouraged — this stops a permissive line-based text reader from
     * "recovering" a fabricated graph out of JSON/XML syntax and outranking the real reader on size. Adds the
     * {@link SignalKind#CONTENT_KIND} signal. For TEXT/UNKNOWN input the structure can't discriminate, so it is neutral.
     */
    private static double contentKindBonus(ContentKind inputKind, GioFileFormat format, boolean extMatch, List<Signal> signals) {
        if (inputKind != ContentKind.JSON && inputKind != ContentKind.XML) {
            return 0.0;
        }
        ContentKind formatKind = contentKindOf(format);
        if (formatKind == inputKind) {
            signals.add(Signal.of(SignalKind.CONTENT_KIND, 0.25, "content is " + inputKind + ", matching this format"));
            return 0.25;
        }
        if (extMatch) {
            // The filename extension already identifies this format; a content-kind guess must not veto it. This
            // matters when a text format borrows JSON/XML-looking syntax, e.g. TriG graph blocks `{ <s> <p> <o> }`.
            return 0.0;
        }
        signals.add(Signal.of(SignalKind.CONTENT_KIND, -1.0,
                "content is " + inputKind + " but " + format.label() + " is a " + formatKind + " format"));
        return -0.6;
    }

    /** Generic id stems that must not act as filename hints (they appear in countless filenames). */
    private static final Set<String> GENERIC_STEMS = Set.of("json", "xml", "rdf", "text", "data", "graph");

    /**
     * Distinctive substrings that, when present in the content, point at a specific format — the
     * {@link SignalKind#VOCABULARY} signal. Keywords are lowercase; only the high-value discriminators are listed
     * (formats with no distinctive vocabulary, e.g. bare N-Triples, are simply absent). Absence is neutral, never a
     * penalty.
     */
    private static final Map<String, List<String>> VOCABULARY = Map.ofEntries(
            Map.entry("graphml", List.of("graphdrawing.org", "<graphml")),
            Map.entry("gexf", List.of("<gexf", "gexf.net")),
            Map.entry("connected-json", List.of("connectedjson", "\"endpoints\"")),
            Map.entry("connected-json5", List.of("connectedjson", "\"endpoints\"")),
            Map.entry("grale", List.of("rankdir", "multigraph", "compound", "\"options\"")),
            Map.entry("json-ld", List.of("@context", "@graph", "@id")),
            Map.entry("turtle", List.of("@prefix", "@base")),
            Map.entry("rdf-xml", List.of("rdf:rdf", "22-rdf-syntax")),
            Map.entry("dot", List.of("digraph", "subgraph", "strict graph", "strict digraph")),
            Map.entry("mermaid", List.of("flowchart", "graph td", "sequencediagram", "statediagram")),
            Map.entry("plantuml", List.of("@startuml")),
            Map.entry("structurizr-dsl", List.of("workspace", "!identifiers")),
            Map.entry("ocif", List.of("ocif", "ocwg")));

    /**
     * Regex vocabulary for formats whose signature is a line shape rather than a fixed keyword. ddot's {@code subject
     * ..predicate.. object} triple syntax (the {@code ..} relation token, e.g. {@code a .... b} or
     * {@code Alice ..knows.. Bob}) is distinctive and must outrank the greedy adjacency-list reader, which otherwise
     * fabricates extra nodes out of the dots.
     */
    private static final Map<String, List<Pattern>> VOCABULARY_PATTERNS = Map.of(
            "ddot", List.of(Pattern.compile("(?m)^\\s*\\S+\\s*\\.\\.\\s*.*?\\.\\.\\s*\\S+\\s*$")),
            // DOT grammar (graphviz.org/doc/info/lang.html): a [strict] (graph|digraph) [id] { … } header, the
            // bracketed [attr=value] lists, and the -- / -> edge operators (spaced, so mermaid's --> does not match).
            "dot", List.of(
                    Pattern.compile("\\b(strict\\s+)?(graph|digraph)\\b[^{};\\n]{0,60}\\{"),
                    Pattern.compile("\\[\\s*[a-z_][a-z0-9_]*\\s*="),
                    Pattern.compile("\\s(--|->)\\s")));

    /**
     * Near-definitive identity markers — a format's root element / namespace. When a marker is present the file IS that
     * format (even if invalid). When ANOTHER signature-format's marker is present and this format's is not, the file
     * belongs to that other format — so a lenient reader must not scavenge it (e.g. GEXF claiming a {@code <graphml>}).
     */
    private static final Map<String, List<String>> SIGNATURES = Map.of(
            "graphml", List.of("graphdrawing.org", "<graphml"),
            "gexf", List.of("gexf.net", "<gexf"),
            "trix", List.of("<trix"),
            "rdf-xml", List.of("rdf:rdf"));

    /** A weak hint when the filename itself names the format (e.g. {@code neo4j_movies.json}, {@code got.graphml}). */
    private static double filenameBonus(String name, GioFileFormat format, List<Signal> signals) {
        String fn = name.toLowerCase(Locale.ROOT);
        String id = format.id();
        String stem = id.contains("-") ? id.substring(0, id.indexOf('-')) : id;
        boolean byId = fn.contains(id);
        boolean byStem = !byId && !GENERIC_STEMS.contains(stem) && stem.length() >= 4 && fn.contains(stem);
        if (byId || byStem) {
            signals.add(Signal.of(SignalKind.FILENAME, 0.1, "filename mentions '" + (byId ? id : stem) + "'"));
            return 0.1;
        }
        return 0.0;
    }

    /** Scans the content head for format-specific keywords and syntax patterns (see {@link #VOCABULARY}). */
    private static double vocabularyBonus(String content, GioFileFormat format, List<Signal> signals) {
        List<String> keywords = VOCABULARY.getOrDefault(format.id(), List.of());
        List<Pattern> patterns = VOCABULARY_PATTERNS.getOrDefault(format.id(), List.of());
        if (keywords.isEmpty() && patterns.isEmpty()) {
            return 0.0;
        }
        String hay = (content.length() > 65536 ? content.substring(0, 65536) : content).toLowerCase(Locale.ROOT);
        List<String> found = new ArrayList<>();
        for (String kw : keywords) {
            if (hay.contains(kw)) {
                found.add(kw);
            }
        }
        int patternHits = (int) patterns.stream().filter(p -> p.matcher(hay).find()).count();
        if (found.isEmpty() && patternHits == 0) {
            return 0.0;
        }
        double bonus = Math.min(0.3, 0.15 * (found.size() + patternHits));
        String desc = String.join(", ", found) + (patternHits > 0
                ? (found.isEmpty() ? "" : " + ") + patternHits + " syntax pattern(s)" : "");
        signals.add(Signal.of(SignalKind.VOCABULARY, bonus, "matched " + format.label() + " vocabulary: " + desc));
        return bonus;
    }

    /**
     * Strong identity score from {@link #SIGNATURES}: a large positive when the content carries this format's marker, a
     * large negative when it carries a DIFFERENT signature-format's marker but not this one's (so a lenient reader does
     * not scavenge another format's file). Neutral otherwise. Drives "looks like X ⇒ is X" even when parsing fails.
     */
    private static double signatureScore(String content, GioFileFormat format, List<Signal> signals) {
        String hay = (content.length() > 65536 ? content.substring(0, 65536) : content).toLowerCase(Locale.ROOT);
        if (SIGNATURES.getOrDefault(format.id(), List.of()).stream().anyMatch(hay::contains)) {
            signals.add(Signal.of(SignalKind.VOCABULARY, 0.4, "carries the " + format.label() + " signature"));
            return 0.4;
        }
        boolean otherSignature = SIGNATURES.entrySet().stream()
                .filter(e -> !e.getKey().equals(format.id()))
                .anyMatch(e -> e.getValue().stream().anyMatch(hay::contains));
        if (otherSignature) {
            signals.add(Signal.of(SignalKind.VOCABULARY, -0.6, "content carries another format's signature, not " + format.label() + "'s"));
            return -0.6;
        }
        return 0.0;
    }

    /** The structural kind a format expects, from its file extensions (with a small override for ids that hide it). */
    private static ContentKind contentKindOf(GioFileFormat format) {
        for (String ext : format.fileExtensions()) {
            String e = ext.toLowerCase(Locale.ROOT);
            if (e.contains("json")) {
                return ContentKind.JSON;
            }
            if (e.contains("xml")) {
                return ContentKind.XML;
            }
        }
        return switch (format.id()) {
            case "trix", "rdf-xml" -> ContentKind.XML;
            case "rdf-json" -> ContentKind.JSON;
            default -> ContentKind.TEXT;
        };
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(0.99, v));
    }

    // -- graph plausibility -----------------------------------------------------------------------------------------

    private static final int PLAUSIBILITY_SAMPLE = 256;
    private static final int MAX_REASONABLE_LABEL = 120;
    /** Structural-syntax characters that should never appear inside a real node id/label (but a misparse leaks them). */
    private static final String STRUCTURAL_CHARS = "{}[]\";";

    /**
     * Structural quality of a parsed graph in [0..1], computed on the format-agnostic CJ model so it scores every
     * recovered candidate identically. A real graph has connected nodes and clean ids/labels; a misparse leaks
     * structural syntax ({@code { } [ ] " ;}) or pure-punctuation tokens (a stray {@code --}) into node ids, or leaves
     * many orphans. URIs and ordinary words stay clean. Lower means less plausible.
     */
    static double graphPlausibility(ICjDocument doc) {
        List<ICjNode> nodes = CjFeature.allNodes(doc).limit(PLAUSIBILITY_SAMPLE).toList();
        if (nodes.isEmpty()) {
            return 1.0;
        }
        Set<String> endpointIds = CjFeature.allEdges(doc)
                .flatMap(ICjEdge::endpoints).map(ICjEndpoint::node).collect(Collectors.toSet());
        boolean hasEdges = !endpointIds.isEmpty();
        int messy = 0;
        int orphans = 0;
        for (ICjNode n : nodes) {
            if (isMessyNode(n)) {
                messy++;
            }
            if (hasEdges && !endpointIds.contains(n.id())) {
                orphans++;
            }
        }
        double messyFrac = (double) messy / nodes.size();
        double orphanFrac = hasEdges ? (double) orphans / nodes.size() : 0.0;
        return clamp01(1.0 - (0.7 * messyFrac + 0.3 * orphanFrac));
    }

    private static boolean isMessyNode(ICjNode n) {
        if (isMessyText(n.id())) {
            return true;
        }
        ICjLabel label = n.label();
        return label != null && label.entries().map(ICjLabelEntry::value).anyMatch(GioInputAnalyzer::isMessyText);
    }

    /** A node id / label value is "messy" if overly long, carries a structural-syntax char, or is pure punctuation. */
    private static boolean isMessyText(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        if (s.length() > MAX_REASONABLE_LABEL) {
            return true;
        }
        boolean hasAlnum = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (STRUCTURAL_CHARS.indexOf(c) >= 0) {
                return true;
            }
            if (Character.isLetterOrDigit(c)) {
                hasAlnum = true;
            }
        }
        return !hasAlnum; // pure-punctuation token, e.g. "--", "//", "->"
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static ConfidenceTier tierOf(List<Candidate> ranked) {
        // Eligible = candidates that parsed (recovered or trivial) and clear the floor; `ranked` is already sorted
        // best-first by confidence.
        List<Candidate> eligible = ranked.stream()
                .filter(c -> c.outcome() != Outcome.SKIPPED)
                .filter(c -> c.confidence() >= GioInputAnalysis.BEST_CONFIDENCE_FLOOR)
                .toList();
        if (eligible.isEmpty()) {
            return ConfidenceTier.UNKNOWN;
        }
        if (eligible.size() == 1) {
            return ConfidenceTier.CONFIDENT;
        }
        double gap = eligible.get(0).confidence() - eligible.get(1).confidence();
        return gap >= CONFIDENT_MARGIN ? ConfidenceTier.CONFIDENT : ConfidenceTier.AMBIGUOUS;
    }

    /**
     * Cheap structural classification of the raw input. Deliberately precise so look-alikes are not misread: a leading
     * <code>{</code>/<code>[</code> is JSON only when the next significant character is JSON-shaped (so TriG/SPARQL
     * graph blocks like <code>{ &lt;s&gt; &lt;p&gt; &lt;o&gt; }</code> stay TEXT), and a leading <code>&lt;</code> is
     * XML only when it begins an XML/markup declaration or an element name (so an RDF <code>&lt;scheme://…&gt;</code>
     * subject stays TEXT). A leading UTF-8 BOM is ignored.
     */
    private static ContentKind sniffContentKind(String content) {
        String s = content.stripLeading();
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
            s = s.substring(1).stripLeading();
        }
        if (s.isEmpty()) {
            return ContentKind.UNKNOWN;
        }
        switch (s.charAt(0)) {
            case '{' -> {
                char next = firstNonWhitespace(s, 1);
                return (next == '"' || next == '}') ? ContentKind.JSON : ContentKind.TEXT;
            }
            case '[' -> {
                return isJsonValueStart(firstNonWhitespace(s, 1)) ? ContentKind.JSON : ContentKind.TEXT;
            }
            case '<' -> {
                if (isUriSubject(s)) {
                    return ContentKind.TEXT; // RDF/N-Triples <scheme://…> subject, not markup
                }
                char next = s.length() > 1 ? s.charAt(1) : '\0';
                boolean markup = next == '?' || next == '!' || next == '_' || next == ':' || Character.isLetter(next);
                return markup ? ContentKind.XML : ContentKind.TEXT;
            }
            default -> {
                return ContentKind.TEXT;
            }
        }
    }

    /** The first non-whitespace char at or after {@code from}, or {@code '\0'} if none. */
    private static char firstNonWhitespace(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return s.charAt(i);
            }
        }
        return '\0';
    }

    /** Characters that may begin a JSON value (after {@code [} or {@code ,}). */
    private static boolean isJsonValueStart(char c) {
        return c == '"' || c == '{' || c == '[' || c == ']' || c == '-' || c == 't' || c == 'f' || c == 'n'
                || (c >= '0' && c <= '9');
    }

    /** A leading {@code <scheme://…>} (only letters/digits/+/./- before {@code ://}) is an RDF URI subject, not markup. */
    private static boolean isUriSubject(String s) {
        int slashSlash = s.indexOf("://");
        if (slashSlash <= 1) {
            return false;
        }
        for (int i = 1; i < slashSlash; i++) {
            char ch = s.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '+' || ch == '.' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    // -- immutable result carriers ----------------------------------------------------------------------------------

    private record AnalysisResult(String inputName, OptionalLong inputSizeBytes, ContentKind contentKind,
                                  boolean deepAnalysisSkipped, List<Candidate> candidates,
                                  ConfidenceTier tier) implements GioInputAnalysis {}

    private record CandidateResult(GioFileFormat format, Outcome outcome, double confidence, Optional<CjAnalysis> stats,
                                   List<Signal> signals, List<ContentError> errors,
                                   String explanation) implements Candidate {}
}
