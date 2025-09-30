package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.util.TextTests;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter.AttributeOrderPerElement;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static com.calpano.graphinout.foundation.xml.XmlTests.wrapInRoot;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * we read both '&quot;' and '"' as '"' using a SAX parser. so when writing, we cannot know what to produce. as a
 * universal, robust solution, we encode it as '{@code &quot;}'.
 */
class Xml2XmlStringWriterTest {

    public record Input_Expected(String input, String expected) {

        public static Input_Expected input_expected(String input, String expected) {
            return new Input_Expected(input, expected);
        }

    }

    ;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlFiles")
    @DisplayName("XML->Writer->XmlString (All XML files)")
    void testAllXml_Xml_Writer_Xml(String displayPath, Path xmlFilePath) throws Exception {
        // == XML -> XmlDocument -> ZML
        Xml2StringWriter xml2string = new Xml2StringWriter();
        // == IN
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        try {
            XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2string);
            if (TestFileUtil.isInvalid(xmlFilePath, "xml")) {
                fail("Expected an exception on an invalid file");
            }
            String xml_out = xml2string.string();
            // XML parsing enforces HTML entity normalisation, otherwise the SAX parser dies
            // so we must adapt out expectations, too
            //xml_in = XmlTool.htmlEntitiesToDecimalEntities(xml_in);

            TestFileUtil.verifyOrRecord(xmlFilePath, xml_out, xml_in, (actual, expected) -> {
                XmlAssert.xAssertThatIsSameXml(actual, expected);
                return true;
            });
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFilePath, "xml")) {
                // perfect, we failed on an invalid file
                return;
            } else {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "\"", "'"})
    void testParseXmlVsSimulateParse(String contentIn) throws Exception {
        Xml2StringWriter xml2string = new Xml2StringWriter(AttributeOrderPerElement.AsWritten, false);

        String xmlIn = wrapInRoot(contentIn);
        // actual SAX parsing
        XmlTool.parseAndWriteXml(xmlIn, xml2string);
        String xmlOut = xml2string.string();

        String contentExpectedOut = XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(contentIn);
        String xmlExpectedOut = wrapInRoot(contentExpectedOut);

        assertThat(xmlOut).isEqualTo(xmlExpectedOut);
    }

    /**
     * Code as a test what the SAX parser does
     *
     * @param in_out pair of string content input and expected result when SAX parsing the content.
     */
    @ParameterizedTest
    @MethodSource("com.calpano.graphinout.foundation.xml.XmlTests#xmlStringsSaxParsed")
    void testParse(Input_Expected in_out) throws Exception {
        String inContent = in_out.input();
        String inXml = wrapInRoot(inContent);

        String expectedContent = in_out.expected();
        String expectedXml = wrapInRoot(expectedContent);


        Xml2StringWriter xml2string = new Xml2StringWriter(AttributeOrderPerElement.AsWritten,
                // use no output encoding
                false);
        XmlTool.parseAndWriteXml(inXml, xml2string);
        String outActualXml = xml2string.string();
        TextTests.xAssertEqual(outActualXml, expectedXml);
    }

    @ParameterizedTest
    @MethodSource("com.calpano.graphinout.foundation.xml.XmlTests#xmlStringsSaxParsed")
    void testSimulatedSaxParse(Input_Expected in_out) throws Exception {
        String inContent = in_out.input();
        String expectedContent = in_out.expected();

        String actual = XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(inContent);
        TextTests.xAssertEqual(actual, expectedContent);
    }


}
