package com.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.graphinout.foundation.util.Texts.CR_13_R;
import static com.graphinout.foundation.util.Texts.LF_10_N;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlToolTest {

    @Test
    public void testNormalizeAttributeSorting() {
        System.out.println("[DEBUG_LOG] Testing attribute sorting");
        String input = "<root z=\"3\" a=\"1\" b=\"2\"><child>content</child></root>";
        String result = XmlNormalizer.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // Attributes should be sorted alphabetically: a, b, z
        assertTrue(result.contains("a=\"1\" b=\"2\" z=\"3\""), "Attributes should be sorted alphabetically");
    }

    @Test
    public void testNormalizeCDATA() {
        System.out.println("[DEBUG_LOG] Testing CDATA handling");
        String input = "<root><![CDATA[Some CDATA content]]></root>";
        String result = XmlNormalizer.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // CDATA should be preserved
        assertTrue(result.contains("<![CDATA[Some CDATA content]]>"), "CDATA content should be preserved");
    }

    @Test
    public void testNormalizeCommentStripping() {
        System.out.println("[DEBUG_LOG] Testing comment stripping");
        String input = "<root><!-- This is a comment --><child>content</child><!-- Another comment --></root>";
        String result = XmlNormalizer.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // Comments should be stripped
        assertFalse(result.contains("<!--"), "Comments should be stripped");
        assertFalse(result.contains("-->"), "Comments should be stripped");
        assertTrue(result.contains("<child>content</child>"), "Content should be preserved");
    }

    @Test
    public void testNormalizeComplexXml() {
        System.out.println("[DEBUG_LOG] Testing complex XML normalization");
        String input = "<root z=\"last\" a=\"first\" m=\"middle\"><!-- comment --><child2 b=\"2\" a=\"1\">text</child2><child1>more text</child1><!-- final comment --></root>";
        String result = XmlNormalizer.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // Check multiple requirements
        assertTrue(result.contains("a=\"first\" m=\"middle\" z=\"last\""), "Root attributes should be sorted");
        assertTrue(result.contains("a=\"1\" b=\"2\""), "Child attributes should be sorted");
        assertFalse(result.contains("<!--"), "Comments should be stripped");
        assertTrue(result.contains("<child2"), "Elements should be preserved");
        assertTrue(result.contains("<child1"), "Elements should be preserved");
    }

    @Test
    public void testNormalizeElementOrder() {
        System.out.println("[DEBUG_LOG] Testing element order preservation");
        String input = "<root><child2>second</child2><child1>first</child1></root>";
        String result = XmlNormalizer.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // Element order should be preserved
        int child2Index = result.indexOf("<child2>");
        int child1Index = result.indexOf("<child1>");
        assertTrue(child2Index < child1Index, "Element order should be preserved");
    }

    @Test
    public void testNormalizeLinebreaks() {
        String input = """
                <root z="3" a="1" b="2">
                  <child>content</child>
                </root>""";
        String expect = """
                <?xml version="1.0" encoding="UTF-8"?>
                <root a="1" b="2" z="3"><child>content</child></root>""";
        String result = XmlNormalizer.normalize(input);
        assertThat(result).isEqualTo(expect);
    }

    @Test
    void testCharRef() {
        String in = "b1b2b3b4b5b6b7b8b9&#13;";
        String out = XmlTool.xmlEncode(in);
        assertThat(out).isEqualTo("b1b2b3b4b5b6b7b8b9&amp;#13;");
    }

    @Test
    void testEncode() {
        String in = """
                M&amp;X xml
                M&fake;X fake
                M&Eacute;X html""";
        assertThat(XmlTool.xmlEncode(in)).isEqualTo("""
                M&amp;amp;X xml
                M&amp;fake;X fake
                M&amp;Eacute;X html""");
        assertThat(XmlTool.xmlDecode(XmlTool.xmlEncode(in))).isEqualTo(in);
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
        assertThat(XmlTool.xmlEncode(in)).isEqualTo("""
                M&amp;X xml
                M&quot;X quot
                M&amp;Eacute;X html
                font-size=&amp;quot;12&amp;quot;
                &amp;lt;nyt_prefs&amp;gt;
                """);
        assertThat(XmlTool.xmlDecode(XmlTool.xmlEncode(in))).isEqualTo(in);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&fake;", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&", "\"", "'", "<", ">"})
    void testEncodeDecode(String s) {
        String enc = XmlTool.xmlEncode(s);
        String dec = XmlTool.xmlDecode(enc);
        assertThat(dec).isEqualTo(s);
    }

    @Test
    void testEncodeXml() {
        String in = """
                M&amp;X
                <data key="d1">M&amp;X</data>
                <data key="d2">M&amp;quot;X</data>
                <data key="d3">M&amp;Eacute;XICO#MEXICO</data>
                """;
        String expected = """
                M&amp;X
                &lt;data key="d1"&gt;M&amp;X&lt;/data&gt;
                &lt;data key="d2"&gt;M&amp;quot;X&lt;/data&gt;
                &lt;data key="d3"&gt;M&amp;Eacute;XICO#MEXICO&lt;/data&gt;
                """;
        XmlAssert.xAssertSameParsedXml(in, expected);
    }

    @Test
    void testNewlinePattern() {
        assertThat(XmlTool.P_TO_LF.matcher("" + CR_13_R).matches()).isTrue();
        assertThat(XmlTool.P_TO_LF.matcher("" + LF_10_N).matches()).isTrue();
        assertThat(XmlTool.P_TO_LF.matcher("" + CR_13_R + LF_10_N).matches()).isTrue();
        String in = "a3aa\r\nbbb\r\nccc\r\n";
        String out = XmlTool.P_TO_LF.matcher(in).replaceAll("*");
        assertThat(out).isEqualTo("a3aa*bbb*ccc*");

        assertThat(XmlTool.P_TO_LF.matcher("&#13;" + CR_13_R).matches()).isTrue();
    }


}
