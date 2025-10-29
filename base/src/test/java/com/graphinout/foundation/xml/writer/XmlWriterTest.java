package com.graphinout.foundation.xml.writer;

import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.IXmlName;
import com.graphinout.foundation.xml.XML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

class XmlWriterTest {

    private final static String FILE_NAME = "test_graph_output.xml";

    @BeforeEach
    public void setUp() throws IOException {
        File targetDir = new File("./target/");
        Arrays.stream(requireNonNull(targetDir.listFiles(pathname -> pathname.getName().startsWith(FILE_NAME)))).sequential().forEach(File::deleteOnExit);
    }

    @Test
    void test() throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        xmlWriter.documentStart();
        xmlWriter.elementStart(IXmlName.of("test"));
        xmlWriter.charactersStart();
        xmlWriter.characters("test", CharactersKind.Default);
        xmlWriter.charactersEnd();
        xmlWriter.elementEnd(IXmlName.of("test"));
        xmlWriter.documentEnd();
        String s = xmlWriter.resultString();
        assertThat(s).isEqualTo(XML.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + "<test>test</test>");
    }

    @Test
    void testCdata() throws IOException {
        // a test XML string with CDATA sections
        String chars = "AAA <![CDATA[BBB]]>CCC<![CDATA[DDD < & > EEE]]>FFF";
        String xml = "<root>" + chars + "</root>";
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        xmlWriter.documentStart();
        xmlWriter.elementStart(IXmlName.of("root"));
        xmlWriter.characterDataWhichMayContainCdata(chars);
        xmlWriter.elementEnd(IXmlName.of("root"));
        xmlWriter.documentEnd();

        String out = xmlWriter.resultString();
        assertThat(out).isEqualTo(XML.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + xml);
    }

}
