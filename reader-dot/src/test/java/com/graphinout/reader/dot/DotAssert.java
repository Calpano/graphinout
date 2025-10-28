package com.graphinout.reader.dot;

import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.foundation.text.StringFormatter;
import com.graphinout.foundation.text.TextReader;
import com.graphinout.foundation.text.TextWriterOnStringBuilder;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;

public class DotAssert {

    /**
     * Normalizes DOT by parsing it into CJ and re-emitting via CjDocument2Dot,
     * then trimming leading/trailing empty lines. This removes formatting and
     * ordering differences that are not structurally significant.
     */
    public static String normalizeDotStrong(String dot) {
        // Parse DOT → CJ
        DotLines2CjDocument parser = new DotLines2CjDocument(null);
        TextReader.read(dot, parser);
        ICjDocument cj = parser.resultDocument();
        // Emit CJ → DOT (structured)
        TextWriterOnStringBuilder out = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(cj, out);
        String s = StringFormatter.normalizeLineBreaks(out.toString());
        return s.trim();
    }

    /**
     * Strict DOT normalizer used for test comparisons.
     * It removes all whitespace (spaces, tabs, CR, LF, etc.) outside
     * double-quoted strings and HTML-like label sections delimited by matching
     * angle brackets. Inside strings ("...") and HTML sections (<...> with
     * proper nesting), the content is preserved verbatim, including spaces and
     * newlines.
     */
    public static String normalizeDot(String dot) {
        StringBuilder out = new StringBuilder(dot.length());
        boolean inQuote = false;
        int htmlDepth = 0; // >0 means we are inside an HTML-like section
        for (int i = 0; i < dot.length(); i++) {
            char c = dot.charAt(i);

            if (inQuote) {
                // Preserve everything inside quotes, honoring escapes
                out.append(c);
                if (c == '\\' && i + 1 < dot.length()) {
                    // keep escaped char as-is
                    out.append(dot.charAt(i + 1));
                    i++;
                } else if (c == '"') {
                    inQuote = false;
                }
                continue;
            }

            if (htmlDepth > 0) {
                // Preserve everything; track nested <...>
                out.append(c);
                if (c == '<') htmlDepth++;
                else if (c == '>') htmlDepth--;
                continue;
            }

            // We are outside of both quote and HTML
            if (c == '"') {
                inQuote = true;
                out.append(c);
                continue;
            }
            if (c == '<') {
                htmlDepth = 1;
                out.append(c);
                continue;
            }

            // Drop all whitespace outside of quoted/HTML sections
            if (Character.isWhitespace(c)) {
                continue;
            }

            out.append(c);
        }
        return out.toString();
    }

    /**
     * Normalizes DOT syntax using the strict whitespace stripper above.
     * @param actualDot actual DOT text
     * @param expectedDot expected DOT text
     * @param extendedDebugInfos optional runnable to print extra diagnostics on mismatch
     */
    public static void xAssertThatIsSameDot(String actualDot, String expectedDot, @Nullable Runnable extendedDebugInfos) {
        String actual = normalizeDot(actualDot);
        String expected = normalizeDot(expectedDot);
        boolean isEqual = actual.equals(expected);
        if (extendedDebugInfos != null && !isEqual) {
            extendedDebugInfos.run();
        }
        assertThat(actual).isEqualTo(expected);
        assert isEqual;
    }

    /**
     * Normalizes DOT syntax using parse/emit cycle.
     * @param actualDot actual DOT text
     * @param expectedDot expected DOT text
     * @param extendedDebugInfos optional runnable to print extra diagnostics on mismatch
     */
    public static void xAssertThatIsSameDotStrong(String actualDot, String expectedDot, @Nullable Runnable extendedDebugInfos) {
        String actual = normalizeDotStrong(actualDot);
        String expected = normalizeDotStrong(expectedDot);
        boolean isEqual = actual.equals(expected);
        if (extendedDebugInfos != null && !isEqual) {
            extendedDebugInfos.run();
        }
        assertThat(actual).isEqualTo(expected);
        assert isEqual;
    }

}
