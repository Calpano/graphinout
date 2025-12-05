package com.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;

class XmlFoundationTest {


    @Test
    void testCharRef() {
        String in = "b1b2b3b4b5b6b7b8b9&#13;";
        String out = XmlFoundation.xmlEncode(in);
        assertThat(out).isEqualTo("b1b2b3b4b5b6b7b8b9&amp;#13;");
    }

    @Test
    void testEncode() {
        String in = """
                M&amp;X xml
                M&fake;X fake
                M&Eacute;X html""";
        assertThat(XmlFoundation.xmlEncode(in)).isEqualTo("""
                M&amp;amp;X xml
                M&amp;fake;X fake
                M&amp;Eacute;X html""");
        assertThat(XmlFoundation.xmlDecode(XmlFoundation.xmlEncode(in))).isEqualTo(in);
    }

    /** This test is just encoding (and decoding). No parsing. */
    @Test
    void testEncode2() {
        String in = """
                M&X xml
                M"X quot
                M&Eacute;X html
                font-size=&quot;12&quot;
                &lt;nyt_prefs&gt;
                """;
        assertThat(XmlFoundation.xmlEncode(in)).isEqualTo("""
                M&amp;X xml
                M&quot;X quot
                M&amp;Eacute;X html
                font-size=&amp;quot;12&amp;quot;
                &amp;lt;nyt_prefs&amp;gt;
                """);
        assertThat(XmlFoundation.xmlDecode(XmlFoundation.xmlEncode(in))).isEqualTo(in);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&fake;", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&", "\"", "'", "<", ">"})
    void testEncodeDecode(String s) {
        String enc = XmlFoundation.xmlEncode(s);
        String dec = XmlFoundation.xmlDecode(enc);
        assertThat(dec).isEqualTo(s);
    }


}
