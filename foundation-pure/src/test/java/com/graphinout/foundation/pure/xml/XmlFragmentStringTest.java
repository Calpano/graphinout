package com.graphinout.foundation.pure.xml;

import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class XmlFragmentStringTest {

    private static String decode(String rawXml) {
        return XmlFragmentString.of(rawXml, XML.XmlSpace.default_).toPlainText();
    }

    @Test
    void plainTextIsEncodedAndDecodedBack() {
        for (String text : new String[]{"", "plain", "A & <B>", "\"q\" 'a'", "already &amp; encoded &eacute;", "]]>", "<![CDATA[x]]>", "[{\"value\":\"<&>\"}]"}) {
            XmlFragmentString xml = XmlFragmentString.ofPlainText(text);
            assertThat(xml.rawXml()).doesNotContain("<");
            assertThat(xml.toPlainText()).isEqualTo(text);
        }
        assertThat(XmlFragmentString.ofPlainText("A & <B>").rawXml()).isEqualTo("A &amp; &lt;B&gt;");
    }

    @Test
    void referencesAndCdataAreDecodedOnce() {
        assertThat(decode("&lt;&gt;&amp;&quot;&apos;")).isEqualTo("<>&\"'");
        assertThat(decode("&amp;lt;")).isEqualTo("&lt;");
        assertThat(decode("&#233;&#xE9;&#x1F600;")).isEqualTo("éé😀");
        assertThat(decode("a <![CDATA[<b> & &amp;]]> &amp; c")).isEqualTo("a <b> & &amp; & c");
    }

    @Test
    void unknownReferencesAreKept() {
        assertThat(decode("a & b; &foo; &#xZZ; &#99999999; &")).isEqualTo("a & b; &foo; &#xZZ; &#99999999; &");
    }

    @Test
    void otherMarkupIsKept() {
        assertThat(decode("a <em>b</em> &amp; c")).isEqualTo("a <em>b</em> &amp; c");
        assertThat(XML.decodeText("a <em>b</em>")).isNull();
    }

    @Test
    void jsonXmlStringOfPlainStringIsEncoded() {
        IJsonXmlString xml = IJsonXmlString.ofPlainString(JavaJsonFactory.INSTANCE, "A & <B>");
        assertThat(xml.rawXmlString()).isEqualTo("A &amp; &lt;B&gt;");
    }

}
