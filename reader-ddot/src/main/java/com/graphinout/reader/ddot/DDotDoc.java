package com.graphinout.reader.ddot;

import java.util.ArrayList;
import java.util.List;

/**
 * Spells a list of triples back out as ddot.it source — the write half of the round-trip that
 * {@link DDotReader} reads back in.
 *
 * <p><strong>Why this is not ddot-core's {@code DdotLineSerializer}.</strong> The parsing side moved
 * wholesale to {@code com.calpano.ddot.it:ddot-core}, but its line serializer is not a substitute for this
 * class, on three counts that would each lose data here:
 * <ul>
 *   <li>it emits only the <em>first</em> metadata entry ("extra keys are dropped", its own javadoc), while
 *       graphinout round-trips a whole property bag plus free text per link;</li>
 *   <li>it spells both a {@code null} type and the literal type {@code links to} as the untyped
 *       {@code from .... to}, so an edge genuinely typed {@code links to} — which is what the
 *       {@code link} / {@code see also} aliases canonicalise to — would come back untyped;</li>
 *   <li>its block form puts the metadata on the {@code !!block?end=…} opening line, which the corpus-pinned
 *       tokenizer no longer accepts: a block opener must END its physical line, so the metadata would turn
 *       the opener into a plain object string. (That is a bug in ddot-core's serializer, reported
 *       separately; it is not exercised by this reader.)</li>
 * </ul>
 * The <em>grammar</em> constraints it must respect are nevertheless ddot-core's, and are cited inline below.
 */
public class DDotDoc {

    public static final String SEPARATOR = " .. ";

    /** Per-link metadata separator (see https://ddot.it): "Meta-data can be appended behind ,,". */
    public static final String META_SEPARATOR = ",,";

    public static class DDotTriple {
        final String subject;
        final String predicate;
        final String object;
        /**
         * Per-link metadata payloads, each rendered inline after a {@code ,,}. A payload is either a
         * structured property {@code ..key.. value} or a free-text note.
         */
        final List<String> meta;

        public DDotTriple(String subject, String predicate, String object) {
            this(subject, predicate, object, new ArrayList<>());
        }

        public DDotTriple(String subject, String predicate, String object, List<String> meta) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            this.meta = meta;
        }
    }

    final List<DDotTriple> triples = new ArrayList<>();

    public String toDDot() {
        StringBuilder b = new StringBuilder();
        String previousSubject = null;
        for (DDotTriple t : triples) {
            // head: full triple, or continuation form when the subject repeats
            if (t.subject.equals(previousSubject)) {
                b.append(SEPARATOR.stripLeading()).append(t.predicate).append(SEPARATOR);
            } else {
                b.append(t.subject).append(SEPARATOR).append(t.predicate).append(SEPARATOR);
            }
            boolean multiLineObject = t.object.indexOf('\n') >= 0;
            // A single `,,` payload rides on the link line; anything else needs the multi-line `,,` block
            // below. Reasons: (a) the parser reads only ONE `,,` per line — the second `,,` is ordinary
            // text and the pairs after it would be silently swallowed (further inline pairs use `;;`, but
            // those cannot follow free text); (b) a `!!block` opener must END its physical line, so
            // metadata can never share the opening line of a block value. See https://ddot.it/block and
            // the parse spec's Meta/MetaNextInline productions.
            boolean inlineMeta = !multiLineObject && t.meta.size() == 1 && t.meta.get(0).indexOf('\n') < 0;

            if (multiLineObject) {
                // A multi-line value is written as a ddot.it/block literal. Content runs until the end marker:
                // a blank line by default, or a custom `?end=MARKER` when the content itself contains a blank
                // line. See https://ddot.it/block.
                String[] blockLines = t.object.split("\n", -1);
                boolean hasBlank = false;
                for (String bl : blockLines) if (bl.isEmpty()) { hasBlank = true; break; }
                String endMarker = null;
                if (hasBlank) {
                    endMarker = "END";
                    boolean collision = true;
                    while (collision) {
                        collision = false;
                        for (String bl : blockLines) if (bl.equals(endMarker)) { collision = true; break; }
                        if (collision) endMarker += "_";
                    }
                }
                b.append(DDotOutput.OBJECT_BLOCK);
                if (endMarker != null) b.append("?end=").append(endMarker);
                b.append('\n');
                for (String blockLine : blockLines) {
                    b.append(blockLine).append('\n');
                }
                b.append(endMarker != null ? endMarker : "").append('\n'); // blank line (default) or custom marker
            } else {
                b.append(t.object);
                if (inlineMeta) b.append(' ').append(META_SEPARATOR).append(' ').append(t.meta.get(0));
                b.append('\n');
            }
            if (!t.meta.isEmpty() && !inlineMeta) {
                // A standalone `,,` opens a metadata block for the link on the line immediately above —
                // which, after a block value, is the line holding its end marker (or its terminating blank
                // line). Each payload is one entry; the block is closed by a second `,,`, without which
                // the whole construct — the link included — would fail to derive.
                b.append(META_SEPARATOR).append('\n');
                for (String payload : t.meta) {
                    b.append(payload).append('\n');
                }
                b.append(META_SEPARATOR).append('\n');
            }
            previousSubject = t.subject;
        }
        return b.toString();
    }
}
