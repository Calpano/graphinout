package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.xml.CharactersKind;
import com.graphinout.foundation.pure.xml.IXmlName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

class XmlContentTest {

    @Test
    void textIsEscaped() {
        XmlContent content = new XmlContent();
        content.addChild((XmlText) XmlText.of("a < b & \"c\" > 'd'", CharactersKind.Default));
        assertThat(content.contentAsXml()).isEqualTo("a &lt; b &amp; &quot;c&quot; &gt; &apos;d&apos;");
    }

    @Test
    void cdataIsKeptAsCdata() {
        XmlContent content = new XmlContent();
        content.addChild((XmlText) XmlText.of("<keep & me>", CharactersKind.CDATA));
        assertThat(content.contentAsXml()).isEqualTo("<![CDATA[<keep & me>]]>");
    }

    @Test
    void rawIsWrittenAsIs() {
        XmlContent content = new XmlContent();
        content.addChild(new XmlRaw("<b>raw &nbsp; & unchecked</b>"));
        assertThat(content.contentAsXml()).isEqualTo("<b>raw &nbsp; & unchecked</b>");
    }

    @Test
    void elementsAreSerializedWithSortedEscapedAttributes() {
        Map<String, String> atts = new LinkedHashMap<>();
        atts.put("z", "1");
        atts.put("a", "x<\"y\"");
        XmlElement em = new XmlElement(null, IXmlName.of("em"), atts);
        em.setContent("t&t", CharactersKind.Default);
        XmlContent content = new XmlContent();
        content.addChild((XmlText) XmlText.of("Hello ", CharactersKind.Default));
        content.addChild(em);
        assertThat(content.contentAsXml()).isEqualTo("Hello <em a=\"x&lt;&quot;y&quot;\" z=\"1\">t&amp;t</em>");
    }

    @Test
    void attributeValuesAreEscapedPerXmlSpec() {
        Map<String, String> atts = new LinkedHashMap<>();
        atts.put("a", "a&b &amp; x>y 'q'\tt\nn\rr");
        XmlContent content = new XmlContent();
        content.addChild(new XmlElement(null, IXmlName.of("e"), atts));
        assertThat(content.contentAsXml()) //
                .isEqualTo("<e a=\"a&amp;b &amp;amp; x>y 'q'&#x9;t&#xA;n&#xD;r\"/>");
    }

}
