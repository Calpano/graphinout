package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;

class NamedEntitiesTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlResources")
    @DisplayName("XML->XmlString (All XML files)")
    void test(String xml) {
        String actual = NamedEntities.htmlEntitiesTo(xml, entityName-> "&" + entityName + ";", entityNum-> "&#" + entityNum + ";");
        assertThat(actual).isEqualTo(xml);
    }

    @Test
    void testHtmlToDecimalEntities1() {
        assertThat(HtmlEntities.getDecimalReplacement("&QUOT;")).isNull();
        assertThat(HtmlEntities.getDecimalReplacement("&quot;")).isNull();
        assertThat(HtmlEntities.contains("Eacute")).isTrue();
        String input = "<root>content &nbsp;foo Café de Paris. &euro;&QUOT;&quot;</root>";
        String result = NamedEntities.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("<root>content &#160;foo Café de Paris. &#8364;&QUOT;&quot;</root>");
    }

    @Test
    void testHtmlToDecimalEntities2() {
        String input = "&QUOT;";
        String result = NamedEntities.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("&QUOT;");
    }

    @Test
    void testHtmlToDecimalEntities3() {
        String input = "<data key=\"d3\">M&Eacute;XICO#MEXICO</data>";
        String result = NamedEntities.htmlEntitiesToDecimalEntities(input);
        assertThat(result).isEqualTo("<data key=\"d3\">M&#201;XICO#MEXICO</data>");
    }

    @Test
    void testHtmlToDecimalEntities4() {
        String raw = """
                    <data key="d1">M&amp;X</data>
                    <data key="d2">M&amp;quot;X</data>
                    <data key="d3">M&amp;Eacute;XICO#MEXICO</data>
                """;
        String xml_in = NamedEntities.htmlEntitiesToDecimalEntities(raw);
        assertThat(xml_in).isEqualTo(raw);
        XmlAssert.xAssertThatIsSameXml(XmlTests.wrapInRoot(raw), XmlTests.wrapInRoot(xml_in));
    }

    @ParameterizedTest
    @ValueSource(strings = {"&0;"})
    void testNegPatterns(String neg) {
        assertThat(NamedEntities.P_REF.matcher(neg).matches()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&fake;"})
    void testPosPatterns(String pos) {
        assertThat(NamedEntities.P_REF.matcher(pos).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"})
    void testXmlPatterns(String pos) {
        assertThat(NamedEntities.P__ENTITYREF.matcher(pos).matches()).isTrue();
    }


}
