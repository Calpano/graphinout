package com.graphinout.base.xml;

import com.graphinout.foundation.pure.xml.document.XmlDocument;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/** Attribute values survive parsing and writing exactly, see {@code XML.xmlEncodeAttributeValue}. */
class AttributeRoundTripTest {

    private static String attribute(String xml) throws Exception {
        return Xml2DocumentWriter.parseToDoc(xml).rootElement().attribute_("a");
    }

    @Test
    void htmlEntityBecomesItsCharacter() throws Exception {
        assertThat(attribute("<r a=\"caf&eacute; &amp; more\"/>")).isEqualTo("café & more");
    }

    @Test
    void writtenValueParsesBackToTheSameValue() throws Exception {
        String value = "café a&b <c> \"q\" 'p'\tt\nn\rr  two spaces";
        String xml = "<r a=\"" + value.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;") //
                .replace("\t", "&#9;").replace("\n", "&#10;").replace("\r", "&#13;") + "\"/>";
        XmlDocument doc = Xml2DocumentWriter.parseToDoc(xml);
        assertThat(doc.rootElement().attribute_("a")).isEqualTo(value);

        String written = doc.toXmlDocumentString();
        assertThat(written).contains("a=\"café a&amp;b &lt;c> &quot;q&quot; 'p'&#x9;t&#xA;n&#xD;r  two spaces\"");
        assertThat(attribute(written)).isEqualTo(value);
    }

}
