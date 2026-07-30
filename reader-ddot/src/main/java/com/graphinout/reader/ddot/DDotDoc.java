package com.graphinout.reader.ddot;

import org.jspecify.annotations.Nullable;

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

    /** Inline separator between further metadata pairs on one line (Parse Spec {@code MetaNextInline}). */
    public static final String META_PAIR_SEPARATOR = ";;";

    /**
     * One metadata payload, kept STRUCTURED rather than pre-rendered.
     * <p>
     * It used to be a ready-made {@code "..key.. value"} string, which made the correct spelling
     * unknowable: whether a payload has to become a {@code !!block} depends on its <em>value</em> alone
     * (a value spanning lines), and whether {@code ;;} may separate it from the next one depends on
     * whether it is a typed pair or free text. With the parts collapsed into one string neither question
     * could be answered, so multi-line and {@code ;;}-bearing values were written in forms that could not
     * be read back.
     *
     * @param key   the property name, or {@code null} for a free-text note ({@code ,, a note})
     * @param value the property value / the note text; may span lines
     */
    public record Meta(@Nullable String key, String value) {

        public static Meta pair(String key, String value) {
            return new Meta(key, value);
        }

        public static Meta text(String value) {
            return new Meta(null, value);
        }

        /**
         * A value spanning lines can only be written as a {@code !!block}: neither an inline payload nor a
         * line inside a {@code ,,} block may contain a newline.
         */
        boolean needsBlock() {
            return value.indexOf('\n') >= 0;
        }

        /**
         * True when this payload can never be written inline, alone or not: a typed value containing
         * {@code ;;} would be cut in two by the very separator that joins pairs, since {@code ;;} after a
         * meta object starts the next pair. Inside a {@code ,,} block the same {@code ;;} is ordinary
         * content, which is the form to use instead (corpus {@code 28-semicolon-in-meta-block}).
         */
        boolean splitsInline() {
            return key != null && value.contains(META_PAIR_SEPARATOR);
        }

        /**
         * True when nothing may follow this payload on its line: free text runs to end of line, so a
         * {@code ;;} after it is swallowed as part of the note rather than starting the next pair.
         */
        boolean blocksFollowers() {
            return key == null;
        }
    }

    public static class DDotTriple {
        final String subject;
        final String predicate;
        final String object;
        /** Per-link metadata payloads, in emission order. */
        final List<Meta> meta;

        public DDotTriple(String subject, String predicate, String object) {
            this(subject, predicate, object, new ArrayList<>());
        }

        public DDotTriple(String subject, String predicate, String object, List<Meta> meta) {
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
            // A SUBJECT spanning lines is only expressible as a `!!block` filling the subject slot: the
            // block puts the subject in scope without producing a triple of its own, and the triple then
            // follows as a continuation line (corpus 29-block-subject / 32-chained-blocks). Writing it raw
            // would leave its first lines as stray prose and give the triple the wrong subject.
            boolean subjectBlock = t.subject.indexOf('\n') >= 0;
            if (subjectBlock) {
                appendBlock(b, t.subject, "");
                b.append(SEPARATOR.stripLeading()).append(t.predicate).append(SEPARATOR);
            } else if (t.subject.equals(previousSubject)) {
                b.append(SEPARATOR.stripLeading()).append(t.predicate).append(SEPARATOR);
            } else {
                b.append(t.subject).append(SEPARATOR).append(t.predicate).append(SEPARATOR);
            }

            // headOpen: the link line is still being written, so metadata may ride on it. A block value
            // closes the line — a `!!block` opener MUST end its physical line — and any metadata then has
            // to continue on the following line.
            boolean headOpen = t.object.indexOf('\n') < 0;
            if (headOpen) {
                b.append(t.object);
            } else {
                appendBlock(b, t.object, DDotOutput.OBJECT_BLOCK);
            }
            appendMeta(b, t.meta, headOpen);
            previousSubject = t.subject;
        }
        return b.toString();
    }

    /**
     * Write {@code value} as a block: the opener (a bare {@code !!block} for a subject block, or
     * {@code ddot.it/block} for a value), the content lines verbatim, then the terminator — a blank line by
     * default, or a generated {@code ?end=MARKER} when the content itself contains a blank line. The opener
     * is always the last thing on its line, which is what makes it an opener at all.
     */
    private static void appendBlock(StringBuilder b, String value, String opener) {
        String[] lines = value.split("\n", -1);
        String endMarker = chooseEndMarker(lines);
        b.append(opener.isEmpty() ? "!!block" : opener);
        if (endMarker != null) b.append("?end=").append(endMarker);
        b.append('\n');
        for (String line : lines) b.append(line).append('\n');
        b.append(endMarker != null ? endMarker : "").append('\n');
    }

    /** A custom end marker when a blank line cannot terminate the block, else {@code null}. */
    private static @Nullable String chooseEndMarker(String[] lines) {
        boolean hasBlank = false;
        for (String line : lines) if (line.isEmpty()) { hasBlank = true; break; }
        if (!hasBlank) return null;
        String marker = "END";
        boolean collision = true;
        while (collision) {
            collision = false;
            for (String line : lines) if (line.equals(marker)) { collision = true; break; }
            if (collision) marker += "_";
        }
        return marker;
    }

    /**
     * Write a link's metadata, choosing the one spelling that can be read back.
     * <p>
     * Three forms exist, and each is the only option for its case:
     * <ul>
     *   <li><strong>inline</strong> {@code ,, ..k.. v ;; ..k2.. v2} — the compact form. Only ONE {@code ,,}
     *       per line is a delimiter, so further pairs need {@code ;;} (corpus 22).</li>
     *   <li><strong>a {@code ,,} block</strong> — needed as soon as free text shares the link with another
     *       payload (text runs to end of line and would swallow a following {@code ;;}), and for a value
     *       containing {@code ;;}, which is ordinary content inside a block (corpus 28).</li>
     *   <li><strong>a {@code !!block} payload</strong> {@code ,, ..k.. !!block} — the only form that can
     *       carry a value spanning lines (corpus 30/34), continued by {@code ;;} on the next line
     *       (corpus 33).</li>
     * </ul>
     */
    private static void appendMeta(StringBuilder b, List<Meta> metas, boolean headOpen) {
        if (metas.isEmpty()) {
            if (headOpen) b.append('\n');
            return;
        }
        boolean anyBlock = metas.stream().anyMatch(Meta::needsBlock);
        if (anyBlock) {
            appendMetaChain(b, metas, headOpen);
            return;
        }
        boolean inlineable = metas.size() == 1
                // alone, free text is fine inline — nothing has to follow it — but a continuation line
                // must start `,,`/`;;` followed by `..`, so there it must be a typed pair
                ? !metas.getFirst().splitsInline() && (headOpen || metas.getFirst().key != null)
                : metas.stream().noneMatch(m -> m.splitsInline() || m.blocksFollowers());
        if (inlineable) {
            if (!headOpen) b.append(META_SEPARATOR).append(' ');
            for (int i = 0; i < metas.size(); i++) {
                if (headOpen) b.append(' ').append(i == 0 ? META_SEPARATOR : META_PAIR_SEPARATOR).append(' ');
                else if (i > 0) b.append(' ').append(META_PAIR_SEPARATOR).append(' ');
                b.append(render(metas.get(i)));
            }
            b.append('\n');
            return;
        }
        // A standalone `,,` block, attached to the link on the line immediately above. Each payload is one
        // entry; the closing `,,` is mandatory — without it the whole construct, link included, fails to
        // derive and the reader drops it.
        if (headOpen) b.append('\n');
        b.append(META_SEPARATOR).append('\n');
        for (Meta m : metas) b.append(render(m)).append('\n');
        b.append(META_SEPARATOR).append('\n');
    }

    /**
     * The {@code !!block} chain: inline-safe pairs ride along, and every payload whose value spans lines
     * ends its line with a {@code !!block} opener whose body is that value. Free text is always blockified
     * here, because inline text would swallow the separator that has to follow it.
     */
    private static void appendMetaChain(StringBuilder b, List<Meta> metas, boolean headOpen) {
        // A payload that must open a block cannot start a continuation line (that requires `,,`/`;;`
        // followed by `..`), so anything blockified has to be reachable while the line is still open.
        List<Meta> ordered = new ArrayList<>(metas);
        ordered.sort((x, y) -> Boolean.compare(x.key != null, y.key != null)); // free text first, stable
        boolean lineOpen = headOpen;
        boolean opened = false;
        for (Meta m : ordered) {
            String separator = opened ? META_PAIR_SEPARATOR : META_SEPARATOR;
            if (lineOpen) b.append(' ').append(separator).append(' ');
            else b.append(separator).append(' ');
            opened = true;
            if (m.key != null) b.append("..").append(m.key).append(".. ");
            if (m.needsBlock() || m.key == null) {
                appendBlock(b, m.value, DDotOutput.OBJECT_BLOCK);
                lineOpen = false;
            } else {
                b.append(m.value);
                lineOpen = true;
            }
        }
        if (lineOpen) b.append('\n');
    }

    /** One payload on its own: a typed pair {@code ..key.. value}, or the bare text of a note. */
    private static String render(Meta m) {
        return m.key == null ? m.value : ".." + m.key + ".. " + m.value;
    }
}
