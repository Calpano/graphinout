package com.graphinout.base.gio;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.gio.GioInputAnalysis.Candidate;
import com.graphinout.base.gio.GioInputAnalysis.Signal;
import com.graphinout.foundation.pure.input.ContentError;

import java.util.List;
import java.util.Locale;

/**
 * Serializes a {@link GioInputAnalysis} to a JSON object — the wire format shared by the {@code detect} CLI command and
 * the {@code /api/detect} HTTP endpoint, so both expose the ranked candidates, parse stats and confidence identically.
 * Dependency-free (hand-rolled) to keep {@code base} lean.
 *
 * <pre>
 * {
 *   "input": "sample.graphml", "sizeBytes": 1234, "contentKind": "XML",
 *   "deepAnalysisSkipped": false, "tier": "CONFIDENT", "best": "graphml",
 *   "candidates": [
 *     { "format": "graphml", "label": "GraphML", "outcome": "RECOVERED", "confidence": 0.720,
 *       "stats": { "graphs": 1, "nodes": 2, "edges": 1, "features": ["directed-edges"] },
 *       "signals": [ { "kind": "EXTENSION", "strength": 0.300, "reason": "…" } ],
 *       "errors": [], "explanation": "Parsed 2 nodes / 1 edges, extension matches" }
 *   ]
 * }
 * </pre>
 */
public final class GioInputAnalysisJson {

    private GioInputAnalysisJson() {}

    public static String toJson(GioInputAnalysis a) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        key(sb, "input").append(str(a.inputName())).append(',');
        key(sb, "sizeBytes").append(a.inputSizeBytes().isPresent() ? Long.toString(a.inputSizeBytes().getAsLong()) : "null").append(',');
        key(sb, "contentKind").append(str(a.contentKind().name())).append(',');
        key(sb, "deepAnalysisSkipped").append(a.deepAnalysisSkipped()).append(',');
        key(sb, "tier").append(str(a.tier().name())).append(',');
        key(sb, "best").append(a.best().map(c -> str(c.format().id())).orElse("null")).append(',');
        key(sb, "candidates").append('[');
        List<Candidate> candidates = a.candidates();
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) sb.append(',');
            candidate(sb, candidates.get(i));
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void candidate(StringBuilder sb, Candidate c) {
        sb.append('{');
        key(sb, "format").append(str(c.format().id())).append(',');
        key(sb, "label").append(str(c.format().label())).append(',');
        key(sb, "outcome").append(str(c.outcome().name())).append(',');
        key(sb, "confidence").append(num(c.confidence())).append(',');
        key(sb, "stats");
        if (c.stats().isPresent()) {
            CjAnalysis s = c.stats().get();
            sb.append("{\"graphs\":").append(s.graphCount())
                    .append(",\"nodes\":").append(s.nodeCount())
                    .append(",\"edges\":").append(s.edgeCount())
                    .append(",\"features\":[");
            List<String> slugs = s.featureSlugs();
            for (int i = 0; i < slugs.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(str(slugs.get(i)));
            }
            sb.append("]}");
        } else {
            sb.append("null");
        }
        sb.append(',');
        key(sb, "signals").append('[');
        List<Signal> signals = c.signals();
        for (int i = 0; i < signals.size(); i++) {
            if (i > 0) sb.append(',');
            Signal sig = signals.get(i);
            sb.append("{\"kind\":").append(str(sig.kind().name()))
                    .append(",\"strength\":").append(num(sig.strength()))
                    .append(",\"reason\":").append(str(sig.reason())).append('}');
        }
        sb.append("],");
        key(sb, "errors").append('[');
        List<ContentError> errors = c.errors();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) sb.append(',');
            ContentError e = errors.get(i);
            sb.append("{\"level\":").append(str(String.valueOf(e.level)))
                    .append(",\"message\":").append(str(e.message == null ? "" : e.message)).append('}');
        }
        sb.append("],");
        key(sb, "explanation").append(str(c.explanation()));
        sb.append('}');
    }

    private static StringBuilder key(StringBuilder sb, String k) {
        return sb.append('"').append(k).append("\":");
    }

    private static String num(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }

    private static String str(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
