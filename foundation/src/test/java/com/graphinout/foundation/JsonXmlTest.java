package com.graphinout.foundation;

import com.graphinout.foundation.json.JSON;
import com.graphinout.foundation.json.value.IJsonXmlString;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class JsonXmlTest {

    public static String xmlFragString = """
            <root xmlns="https://www.example.org/myNamespace">
            Hello <em>beautiful</em>
                   World! &amp;quot;
            </root>""";


    @Test
    void testXmlFragmentString2Json() {
        XmlFragmentString xmlFragmentString = XmlFragmentString.of(xmlFragString, XML.XmlSpace.preserve);
        IJsonXmlString jsonXml = IJsonXmlString.of(JavaJsonFactory.INSTANCE, xmlFragmentString);
        assertThat(jsonXml.xmlSpace()).isEqualTo(JSON.XmlSpace.preserve);
        assertThat(jsonXml.rawXmlString()).isEqualTo(xmlFragString);
    }

}
