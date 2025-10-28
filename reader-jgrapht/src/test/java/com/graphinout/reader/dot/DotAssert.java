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
    public static String normalizeDot(String dot) {
        // Parse DOT → CJ
        DotLines2CjDocument parser = new DotLines2CjDocument();
        TextReader.read(dot, parser);
        ICjDocument cj = parser.resultDocument();
        // Emit CJ → DOT (structured)
        TextWriterOnStringBuilder out = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(cj, out);
        String s = StringFormatter.normalizeLineBreaks(out.toString());
        return s.trim();
    }

    /**
     * Normalizes DOT syntax internally by parsing/emitting.
     * @param actualDot
     * @param expectedDot
     * @param extendedDebugInfos
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

}
