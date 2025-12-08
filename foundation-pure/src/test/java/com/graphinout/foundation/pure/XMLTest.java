package com.graphinout.foundation.pure;

import com.graphinout.foundation.pure.xml.XML;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;

class XMLTest {


    @Test
    void testCharRef() {
        String in = "b1b2b3b4b5b6b7b8b9&#13;";
        String out = XML.xmlEncode(in);
        assertThat(out).isEqualTo("b1b2b3b4b5b6b7b8b9&amp;#13;");
    }

    @Test
    void testEncode() {
        String in = "M&amp;X xml\n" +
                "M&fake;X fake\n" +
                "M&Eacute;X html";
        assertThat(XML.xmlEncode(in)).isEqualTo("M&amp;amp;X xml\n" +
                "M&amp;fake;X fake\n" +
                "M&amp;Eacute;X html");
        assertThat(XML.xmlDecode(XML.xmlEncode(in))).isEqualTo(in);
    }

    /** This test is just encoding (and decoding). No parsing. */
    @Test
    void testEncode2() {
        String in = "M&X xml\n" +
                "M\"X quot\n" +
                "M&Eacute;X html\n" +
                "font-size=&quot;12&quot;\n" +
                "&lt;nyt_prefs&gt;\n";
        assertThat(XML.xmlEncode(in)).isEqualTo("M&amp;X xml\n" +
                "M&quot;X quot\n" +
                "M&amp;Eacute;X html\n" +
                "font-size=&amp;quot;12&amp;quot;\n" +
                "&amp;lt;nyt_prefs&amp;gt;\n");
        assertThat(XML.xmlDecode(XML.xmlEncode(in))).isEqualTo(in);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&fake;", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&", "\"", "'", "<", ">"})
    void testEncodeDecode(String s) {
        String enc = XML.xmlEncode(s);
        String dec = XML.xmlDecode(enc);
        assertThat(dec).isEqualTo(s);
    }


}
