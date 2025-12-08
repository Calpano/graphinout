package com.graphinout.foundation.pure;

import com.graphinout.foundation.pure.json.JSON;
import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class JsonXmlTest {

    public static String xmlFragString = "<root xmlns=\"https://www.example.org/myNamespace\">\n" +
            "Hello <em>beautiful</em>\n" +
            "       World! &amp;quot;\n" +
            "</root>";


    @Test
    void testXmlFragmentString2Json() {
        XmlFragmentString xmlFragmentString = XmlFragmentString.of(xmlFragString, XML.XmlSpace.preserve);
        IJsonXmlString jsonXml = IJsonXmlString.of(JavaJsonFactory.INSTANCE, xmlFragmentString);
        assertThat(jsonXml.xmlSpace()).isEqualTo(JSON.XmlSpace.preserve);
        assertThat(jsonXml.rawXmlString()).isEqualTo(xmlFragString);
    }

}
