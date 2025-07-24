package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlToolTest {

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
        String expect = "<root a=\"1\" b=\"2\" z=\"3\"><child>content</child></root>";
        String result = XmlFormatter.normalize(input);
        assertThat(result).isEqualTo(expect);
    }

}
