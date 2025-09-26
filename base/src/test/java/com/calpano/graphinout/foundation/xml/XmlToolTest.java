package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlToolTest {
    @Test
    void testEntities2() {
        String input = "&QUOT;";
        String result = XmlTool.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("&QUOT;");
    }
    @Test
    void testEntities3() {
        String input = "<data key=\"d3\">M&Eacute;XICO#MEXICO</data>";
        String result = XmlTool.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("<data key=\"d3\">M&#201;XICO#MEXICO</data>");
    }

    @Test
    void testEntities() {
        assertThat(HtmlEntities.getReplacement("&QUOT;")).isNull();
        assertThat(HtmlEntities.getReplacement("&quot;")).isNull();
        assertThat(HtmlEntities.contains("Eacute")).isTrue();
        String input = "<root>content &nbsp;foo Café de Paris. &euro;&QUOT;&quot;</root>";
        String result = XmlTool.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("<root>content &#160;foo Café de Paris. &#8364;&QUOT;&quot;</root>");
    }

    @Test
    public void testNormalizeAttributeSorting() {
        System.out.println("[DEBUG_LOG] Testing attribute sorting");
        String input = "<root z=\"3\" a=\"1\" b=\"2\"><child>content</child></root>";
        String result = XmlFormatter.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // Attributes should be sorted alphabetically: a, b, z
        assertTrue(result.contains("a=\"1\" b=\"2\" z=\"3\""), "Attributes should be sorted alphabetically");
    }

    @Test
    public void testNormalizeCDATA() {
        System.out.println("[DEBUG_LOG] Testing CDATA handling");
        String input = "<root><![CDATA[Some CDATA content]]></root>";
        String result = XmlFormatter.normalize(input);
        System.out.println("[DEBUG_LOG] Input:  " + input);
        System.out.println("[DEBUG_LOG] Output: " + result);

        // CDATA should be preserved
        assertTrue(result.contains("<![CDATA[Some CDATA content]]>"), "CDATA content should be preserved");
    }

    @Test
    public void testNormalizeCommentStripping() {
        System.out.println("[DEBUG_LOG] Testing comment stripping");
        String input = "<root><!-- This is a comment --><child>content</child><!-- Another comment --></root>";
        String result = XmlFormatter.normalize(input);
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
        String result = XmlFormatter.normalize(input);
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
        String result = XmlFormatter.normalize(input);
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
        String expect = XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + "<root a=\"1\" b=\"2\" z=\"3\"><child>content</child></root>";
        String result = XmlFormatter.normalize(input);
        assertThat(result).isEqualTo(expect);
    }

    @Test
    void testEncode() {
        String in = """
                M&amp;X xml
                M&fake;X fake
                M&Eacute;X html""";
        assertThat(XmlTool.xmlEncode(in)).isEqualTo("""
                M&amp;X xml
                M&fake;X fake
                M&Eacute;X html""");
        // decode would process "&amp" to "&"
        assertThat(XmlTool.xmlEncode(in)).isEqualTo(in);
    }

    @Test
    void testEncodeXml() {
        String in = """
                <data key="d1">M&amp;X</data>
                <data key="d2">M&amp;quot;X</data>
                <data key="d3">M&amp;Eacute;XICO#MEXICO</data>
                """;
        assertThat(XmlTool.xmlEncode(in)).isEqualTo("""
                &lt;data key="d1"&gt;M&amp;X&lt;/data&gt;
                &lt;data key="d2"&gt;M&amp;quot;X&lt;/data&gt;
                &lt;data key="d3"&gt;M&amp;Eacute;XICO#MEXICO&lt;/data&gt;
                """);
    }

    @ParameterizedTest
    @ValueSource(strings = {"&0;"})
    void testNegPatterns(String neg) {
        assertThat(XmlTool.P_REF.matcher(neg).matches()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&fake;"})
    void testPosPatterns(String pos) {
        assertThat(XmlTool.P_REF.matcher(pos).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"})
    void testXmlPatterns(String pos) {
        assertThat(XmlTool.P_ENTITYREF.matcher(pos).matches()).isTrue();
    }

    @Test
    void testCharRef() {
        String in = "b1b2b3b4b5b6b7b8b9&#13;";
        String out = XmlTool.xmlEncode(in);
        assertThat(out).isEqualTo(in);
    }


}
