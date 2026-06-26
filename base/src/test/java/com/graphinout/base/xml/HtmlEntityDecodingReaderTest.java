package com.graphinout.base.xml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static com.google.common.truth.Truth.assertThat;

class HtmlEntityDecodingReaderTest {

    /** Read everything through the decoder using a fixed buffer size (to exercise chunk boundaries). */
    private static String decode(String input, int bufSize) throws IOException {
        StringBuilder out = new StringBuilder();
        char[] buf = new char[bufSize];
        try (HtmlEntityDecodingReader r = new HtmlEntityDecodingReader(new StringReader(input))) {
            int n;
            while ((n = r.read(buf, 0, buf.length)) != -1) {
                out.append(buf, 0, n);
            }
        }
        return out.toString();
    }

    /** Same input must decode identically no matter how the reads chop it up (true streaming, bounded lookahead). */
    private static String decodeAllSizes(String input) throws IOException {
        String ref = decode(input, 4096);
        for (int size : new int[]{1, 2, 3, 7, 64}) {
            assertThat(decode(input, size)).isEqualTo(ref);
        }
        return ref;
    }

    @Test
    void rewritesHtmlNamedEntities() throws IOException {
        assertThat(decodeAllSizes("M&Eacute;XICO")).isEqualTo("M&#201;XICO");
        assertThat(decodeAllSizes("a&nbsp;b")).isEqualTo("a&#160;b");
    }

    @Test
    void keepsXmlAndNumericEntities() throws IOException {
        assertThat(decodeAllSizes("a &amp; b &lt;c&gt; &quot;d&quot;")).isEqualTo("a &amp; b &lt;c&gt; &quot;d&quot;");
        assertThat(decodeAllSizes("&#201; &#xE9;")).isEqualTo("&#201; &#xE9;");
    }

    @Test
    void leavesUnknownAndBareAmpersandUntouched() throws IOException {
        assertThat(decodeAllSizes("&notAnEntity;")).isEqualTo("&notAnEntity;");
        assertThat(decodeAllSizes("rock & roll")).isEqualTo("rock & roll");
        assertThat(decodeAllSizes("trailing &")).isEqualTo("trailing &");
    }

    @Test
    void mixedContentAndPlainText() throws IOException {
        assertThat(decodeAllSizes("plain text, no entities")).isEqualTo("plain text, no entities");
        assertThat(decodeAllSizes("M&Eacute;XICO &amp; caf&eacute; &nbsp;end"))
                .isEqualTo("M&#201;XICO &amp; caf&#233; &#160;end");
        assertThat(decodeAllSizes("")).isEqualTo("");
    }
}
