package com.graphinout.base.xml;

import com.graphinout.foundation.pure.input.ContentError;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A streaming {@link Reader} decorator that rewrites HTML named entities to numeric character references on the fly, so
 * a strict XML/SAX parser can consume HTML-flavoured XML (e.g. {@code &Eacute;}, {@code &nbsp;}) without choking on
 * "entity not declared". It is the streaming counterpart of {@link NamedEntities#htmlEntitiesToDecimalEntities} — no
 * temp file, no whole-document buffer: an entity reference {@code &name;} is short and bounded, so only the few
 * characters of the current token are ever held.
 *
 * <p>Rewrite rules (identical to {@link NamedEntities#htmlEntitiesTo}):
 * <ul>
 *   <li>XML's five predefined entities ({@code &amp; &lt; &gt; &quot; &apos;}) — passed through verbatim;</li>
 *   <li>numeric character references ({@code &#233;}, {@code &#xE9;}) — passed through verbatim;</li>
 *   <li>a known HTML named entity ({@code &Eacute;}) — replaced by its decimal char ref ({@code &#201;});</li>
 *   <li>anything else (unterminated {@code &}, unknown name) — left exactly as read (lenient).</li>
 * </ul>
 */
public final class HtmlEntityDecodingReader extends Reader {

    /** Longest HTML entity name (CounterClockwiseContourIntegral) is 31 chars; cap the lookahead a little above that. */
    private static final int MAX_NAME_LENGTH = 40;
    private static final int RUN_CHUNK = 4096;
    private static final Set<String> XML_ENTITIES = Set.of("amp", "lt", "gt", "quot", "apos");

    private final Reader in;
    private final LinkedHashSet<String> rewritten = new LinkedHashSet<>();
    private String pending = "";
    private int pendingPos = 0;
    private int pushback = -2; // -2 = nothing pushed back
    private boolean closed = false;

    public HtmlEntityDecodingReader(Reader in) {
        this.in = in instanceof BufferedReader ? in : new BufferedReader(in);
    }

    /**
     * The distinct HTML named entities this reader has rewritten so far (e.g. {@code "Eacute"}), in first-seen order.
     * Non-empty means the input was not well-formed XML and was auto-corrected — a caller may surface this as a warning.
     */
    public Set<String> rewrittenEntityNames() {
        return Collections.unmodifiableSet(rewritten);
    }

    /**
     * A {@link ContentError.ErrorLevel#Warn Warn}-level error summarising the auto-corrections, or {@code null} if the
     * input needed none. Readers should surface this so a silently-malformed file is still flagged.
     */
    public @Nullable ContentError autoCorrectionWarning() {
        if (rewritten.isEmpty()) {
            return null;
        }
        String list = rewritten.stream().map(n -> "&" + n + ";").collect(Collectors.joining(", "));
        return ContentError.of(ContentError.ErrorLevel.Warn,
                "Auto-corrected " + rewritten.size() + " undeclared HTML " + (rewritten.size() == 1 ? "entity" : "entities")
                        + " to numeric character references: " + list + " (input was not well-formed XML)");
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (off < 0 || len < 0 || off + len > cbuf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int written = 0;
        while (written < len) {
            if (pendingPos >= pending.length() && !refill()) {
                break;
            }
            int n = Math.min(len - written, pending.length() - pendingPos);
            pending.getChars(pendingPos, pendingPos + n, cbuf, off + written);
            pendingPos += n;
            written += n;
        }
        return written == 0 ? -1 : written;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        in.close();
    }

    // -- internals ---------------------------------------------------------------------------------------------------

    private int nextChar() throws IOException {
        if (pushback != -2) {
            int c = pushback;
            pushback = -2;
            return c;
        }
        return in.read();
    }

    private void unread(int c) {
        pushback = c;
    }

    /** Refill {@link #pending} with the next entity replacement or a run of plain characters. False at EOF. */
    private boolean refill() throws IOException {
        pendingPos = 0;
        int c = nextChar();
        if (c == -1) {
            pending = "";
            return false;
        }
        if (c == '&') {
            pending = decodeEntity();
            return true;
        }
        StringBuilder run = new StringBuilder().append((char) c);
        while (run.length() < RUN_CHUNK) {
            c = nextChar();
            if (c == -1) {
                break;
            }
            if (c == '&') {
                unread(c);
                break;
            }
            run.append((char) c);
        }
        pending = run.toString();
        return true;
    }

    /** Called right after the opening {@code &} has been consumed. Returns the text to emit. */
    private String decodeEntity() throws IOException {
        int c = nextChar();
        if (c == '#') { // numeric character reference: emit verbatim
            StringBuilder ref = new StringBuilder("&#");
            c = nextChar();
            while (c != -1 && c != ';' && isRefChar(c) && ref.length() < MAX_NAME_LENGTH) {
                ref.append((char) c);
                c = nextChar();
            }
            if (c == ';') {
                return ref.append(';').toString();
            }
            if (c != -1) {
                unread(c);
            }
            return ref.toString(); // unterminated — leave as read
        }
        StringBuilder nameBuf = new StringBuilder();
        while (c != -1 && isNameChar(c) && nameBuf.length() < MAX_NAME_LENGTH) {
            nameBuf.append((char) c);
            c = nextChar();
        }
        String name = nameBuf.toString();
        if (c == ';') {
            if (XML_ENTITIES.contains(name.toLowerCase(Locale.ROOT))) {
                return "&" + name + ";"; // XML predefined — keep
            }
            String decimal = HtmlEntities.getDecimalReplacement(name);
            if (decimal != null) {
                rewritten.add(name); // known HTML entity -> &#N;
                return decimal;
            }
            return "&" + name + ";"; // unknown named entity: leave verbatim
        }
        if (c != -1) {
            unread(c); // a bare '&' (not a real entity) — emit it literally, reprocess the terminator
        }
        return "&" + name;
    }

    private static boolean isNameChar(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private static boolean isRefChar(int c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == 'x' || c == 'X';
    }
}
