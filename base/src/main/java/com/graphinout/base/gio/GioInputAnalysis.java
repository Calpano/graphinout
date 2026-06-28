package com.graphinout.base.gio;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.foundation.pure.input.ContentError;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Result of analyzing a single input to decide which format(s) it most likely is.
 * <p>
 * Instead of trusting one detector with a yes/no answer, the engine probes every candidate
 * {@link GioReader} and ranks them by <em>what they actually recover</em>, not merely whether
 * they parse. The three coarse outcomes (see {@link Outcome}) are:
 * <ul>
 *   <li>a hard parse error eliminates a candidate,</li>
 *   <li>a clean parse that yields an empty/trivial graph (0 nodes) is de-ranked as
 *       "probably not this" — the canonical broken-graph6 case, where a reader happily
 *       consumes the bytes but produces nothing meaningful,</li>
 *   <li>a parse into a real graph is scored by what it recovered: nodes, edges and the
 *       {@link CjAnalysis#features() features} it exhibits.</li>
 * </ul>
 * Every contributing reason is captured as a {@link Signal} on the {@link Candidate} so the
 * ranking is explainable and a silent wrong conversion can never happen.
 * <p>
 * This type is purely a <em>result carrier</em>: it hosts no detection policy and is meant to be
 * serialized as-is (e.g. by a {@code /api/detect} endpoint or a {@code detect} CLI subcommand)
 * and consumed by a UI that renders the {@link #tier() confidence tier} without any detection
 * logic of its own. The recovered statistics reuse the shared {@link CjAnalysis} /
 * {@link com.graphinout.base.cj.analyze.CjFeature} vocabulary, so detection speaks the same
 * feature language as the rest of the engine.
 *
 * @see GioReader
 * @see GioFileFormat
 */
public interface GioInputAnalysis {

    /**
     * Name of the analyzed input (typically a filename or path), as reported by the input source.
     * Used for filename- and extension-based signals.
     */
    String inputName();

    /**
     * Size of the input in bytes, if known. Empty when the input is streamed and not measurable
     * up front. Drives the large-file policy: above a threshold (e.g. 1&nbsp;MB) the engine skips
     * the expensive reader-level parse and falls back to cheap structural signals only — such
     * candidates carry {@link Outcome#SKIPPED}.
     */
    OptionalLong inputSizeBytes();

    /**
     * Cheap structural classification of the raw bytes, computed before any reader runs.
     * Narrows the candidate set and seeds {@link SignalKind#CONTENT_KIND} signals.
     */
    ContentKind contentKind();

    /**
     * True when reader-level parsing was skipped for all candidates (e.g. the input exceeded the
     * large-file threshold, or was an archive containing multiple graphs). When set, ranking rests
     * on structural signals alone and confidence is necessarily weaker.
     */
    boolean deepAnalysisSkipped();

    /**
     * All probed candidates, ordered best-first (highest {@link Candidate#confidence()} first).
     * Includes eliminated and de-ranked candidates so callers can show <em>why</em> a format was
     * rejected, not just which one won.
     */
    List<Candidate> candidates();

    /** A candidate below this confidence is too weak to be called "the detected format". */
    double BEST_CONFIDENCE_FLOOR = 0.4;

    /**
     * The most likely format — by identity, not merely by parseability. The highest-confidence candidate whose markers
     * (extension, content kind, vocabulary/signature) identify the input, whether it {@linkplain Outcome#RECOVERED
     * parsed}, parsed to {@linkplain Outcome#TRIVIAL nothing}, or could not be parsed at all
     * ({@linkplain Outcome#ELIMINATED} — "looks like this format but is invalid"). A clean parse outranks a salvaged or
     * unparseable one of the same strength. Empty when nothing clears {@link #BEST_CONFIDENCE_FLOOR} (the
     * "couldn't tell — pick a format" case). {@linkplain Outcome#SKIPPED Skipped} candidates are never returned.
     */
    default Optional<Candidate> best() {
        return candidates().stream()
                .filter(c -> c.outcome() != Outcome.SKIPPED)
                .filter(c -> c.confidence() >= BEST_CONFIDENCE_FLOOR)
                .max(Comparator.comparingDouble(Candidate::confidence));
    }

    /**
     * Confidence tier derived from the ranked candidates, intended to drive the UI directly:
     * a confident pill, a slim disambiguation ribbon, or a "pick a format" fallback.
     * Implementations may override the thresholds; the default treats a clear single winner as
     * {@link ConfidenceTier#CONFIDENT}, a small cluster of close scores as
     * {@link ConfidenceTier#AMBIGUOUS}, and nothing recovered as {@link ConfidenceTier#UNKNOWN}.
     */
    ConfidenceTier tier();

    // ------------------------------------------------------------------------------------------

    /**
     * One reader's verdict on the input: its format, outcome, recovered statistics, the signals
     * that justify its score, and any errors raised while parsing.
     */
    interface Candidate {

        /** The format this candidate represents (id, label, extensions). */
        GioFileFormat format();

        /** Coarse classification of how this candidate fared. */
        Outcome outcome();

        /**
         * Normalized confidence in [0..1] that the input <em>is</em> this format, aggregated from
         * all {@link #signals()}. Eliminated candidates score ~0; a lone clean recovery scores
         * near 1. This is the value the candidate list is sorted by.
         */
        double confidence();

        /**
         * What the reader recovered, as the shared {@link CjAnalysis} (graph/node/edge counts plus
         * the set of {@link com.graphinout.base.cj.analyze.CjFeature features} present). Present
         * whenever the reader actually parsed — a {@link CjAnalysis#nodeCount() nodeCount} of 0
         * marks the {@link Outcome#TRIVIAL trivial} case; empty when the candidate was
         * {@link Outcome#ELIMINATED} or {@link Outcome#SKIPPED} before producing a graph.
         */
        Optional<CjAnalysis> stats();

        /**
         * The individual reasons contributing to {@link #confidence()}, each in [-1..1].
         * Surfacing these makes the ranking auditable — e.g. "+0.8 parsed 42 nodes / 60 edges",
         * "-1.0 parsed but graph is empty", "+0.3 extension .graphml matches".
         */
        List<Signal> signals();

        /**
         * Errors and warnings emitted by the reader while parsing this input. A non-empty list of
         * {@link ContentError.ErrorLevel#Error}-level entries typically accompanies
         * {@link Outcome#ELIMINATED}.
         */
        List<ContentError> errors();

        /**
         * Short human-readable explanation of this candidate's outcome and rank, suitable for a
         * tooltip or CLI line (e.g. "Parsed but graph is empty — probably not Graph6").
         */
        String explanation();
    }

    // ------------------------------------------------------------------------------------------

    /**
     * One explainable reason for or against a candidate format, valued in [-1..1] per the design's
     * signal model: {@code -1} strongly discouraging, {@code 0} no information, {@code +1} strongly
     * encouraging.
     *
     * @param kind     which class of evidence produced this signal
     * @param strength confidence contribution in [-1..1]
     * @param reason   short human-readable justification, surfaced in UI and CLI output
     */
    record Signal(SignalKind kind, double strength, String reason) {

        public static Signal of(SignalKind kind, double strength, String reason) {
            return new Signal(kind, Math.max(-1.0, Math.min(1.0, strength)), reason);
        }

        public boolean isEncouraging() {
            return strength > 0;
        }

        public boolean isDiscouraging() {
            return strength < 0;
        }
    }

    // ------------------------------------------------------------------------------------------

    /** Coarse classification of how a {@link Candidate} fared against the input. */
    enum Outcome {
        /** Reader hit a hard parse error (or threw): this format is ruled out. */
        ELIMINATED,
        /** Reader parsed cleanly but recovered an empty/trivial graph: de-ranked as "probably not this". */
        TRIVIAL,
        /** Reader parsed the input into a real, non-trivial graph: scored by what it recovered. */
        RECOVERED,
        /** Reader was not attempted (large-file threshold, archive policy, or early exclusion). */
        SKIPPED
    }

    /** Confidence tier of the whole analysis, mapping directly to a UI presentation. */
    enum ConfidenceTier {
        /** Exactly one strong candidate — show a confident pill ("Detected: GraphML ✓"). */
        CONFIDENT,
        /** A few close candidates — show a slim disambiguation ribbon with one-click confirm. */
        AMBIGUOUS,
        /** Nothing recovered — show "Couldn't tell — pick a format" with search. */
        UNKNOWN
    }

    /** Cheap structural classification of the raw input bytes, computed before any reader runs. */
    enum ContentKind {
        XML,
        JSON,
        TEXT,
        BINARY,
        UNKNOWN
    }

    /** The class of evidence behind a {@link Signal}, mirroring the engine- and reader-level checks. */
    enum SignalKind {
        /** The filename hints at a tool or format name. */
        FILENAME,
        /** The file extension matches a format's registered extensions. */
        EXTENSION,
        /** Magic bytes identify the format (rare). */
        MAGIC_BYTES,
        /** The raw bytes are/aren't the structural kind (XML/JSON/TEXT) the format requires. */
        CONTENT_KIND,
        /** Format-specific vocabulary/keywords were found in the content. */
        VOCABULARY,
        /** The reader parsed (or failed to parse) the input. */
        PARSE,
        /** The size of the recovered graph (nodes/edges) supports or undermines the candidate. */
        GRAPH_SIZE,
        /** Recovered features are characteristic (or uncharacteristic) of the format. */
        FEATURES,
        /** Structural quality of the recovered graph (connectedness, clean ids/labels) — a misparse looks implausible. */
        PLAUSIBILITY,
        /** Any other heuristic. */
        OTHER
    }
}
