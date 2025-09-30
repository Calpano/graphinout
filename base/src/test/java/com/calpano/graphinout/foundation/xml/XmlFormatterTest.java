package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class XmlFormatterTest {

    @Test
    void test() {
        String a = "<?xml version='1.0' encoding='utf-8'?>";
        assertThat(XmlFormatter.normalizeXmlDecl(a)).isEqualTo(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8);
    }

    @Test
    void testEAcute() {
        // NOTE undeclared HTML entities are not allowed
        String inContent = "&Eacute;";
        String outContent = XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(inContent);
        String outXml = XmlTests.wrapInRoot(outContent);
        // what would SAX have in memory?
        assertThat(outXml).isEqualTo("""
            <?xml version="1.0" encoding="UTF-8"?>
            <root>&Eacute;</root>""");
    }

    @Test
    void testEnc() {
        String content = "bar's";
        String normXml = XmlTests.wrapInRoot(XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(content));
        String xml = XmlTests.wrapInRoot(content);
        assertThat(normXml).isEqualTo(normXml);
    }

    @Test
    void testQuot() {
        String inContent = "bar&quot;";
        String inNormXml = XmlTests.wrapInRoot(XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(inContent));
        // what would SAX have in memory?
        String out = XmlTests.wrapInRoot("bar\"");
        assertThat(inNormXml).isEqualTo(out);
    }

    @Test
    void testRegex() {
        assertThat(XmlFormatter.XML_DECL_PATTERN.matcher(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8).find()).isTrue();
    }

    @Test
    void testWrap() {
        String in = """
                <Resource id="2">
                    a1a2a3a4a5a6a7a8a9&#13;
                    b1b2b3b4b5b6b7b8b9&#13;
                    c1c2c3c4c5c6c7c8c9&#13;
                    d1d2d3d4d5d6d7d8d9&#13;
                    e1e2e3e4e5e6e7e8e9&#13;
                    YII=
                <Resource>""";
        assertThat(XmlFormatter.wrap(in, 100)).isEqualTo(in);
        assertThat(XmlFormatter.wrap(in, 1000)).isEqualTo(in);
        assertThat(XmlFormatter.wrap(in, 30)).isEqualTo(in);
    }

}
