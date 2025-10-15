package com.graphinout.foundation.xml;

import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.util.TextTests;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.graphinout.foundation.xml.XmlTests.wrapInRoot;
import static com.google.common.truth.Truth.assertThat;
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

    private static final String TEST_ID = "Xml2String";

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#xmlResources")
    @DisplayName("XML->Writer->XmlString (All XML files)")
    void testAllXml_Xml_Writer_Xml(String displayPath, Resource xmlResource) throws Exception {
        // == XML -> XmlDocument -> ZML
        Xml2StringWriter xml2string = new Xml2StringWriter();
        // == IN
        String xml_in = xmlResource.getContentAsString();
        try {
            XmlTool.parseAndWriteXml(xmlResource, xml2string);
            if (TestFileUtil.isInvalid(xmlResource, "xml")) {
                fail("Expected an exception on an invalid file");
            }
            String xml_out = xml2string.resultString();
            // XML parsing enforces HTML entity normalisation, otherwise the SAX parser dies
            // so we must adapt out expectations, too
            //xml_in = XmlTool.htmlEntitiesToDecimalEntities(xml_in);

            TestFileUtil.verifyOrRecord(xmlResource, TEST_ID, xml_out, xml_in, (actual, expected) -> {
                XmlAssert.xAssertThatIsSameXml(actual, expected);
                return true;
            }, s -> s);
        } catch (Throwable e) {
            //noinspection StatementWithEmptyBody
            if (TestFileUtil.isInvalid(xmlResource, "xml")) {
                // perfect, we failed on an invalid file
            } else {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "\"", "'"})
    void testParseXmlVsSimulateParse(String contentIn) throws Exception {
        Xml2StringWriter xml2string = new Xml2StringWriter(XML.AttributeOrderPerElement.AsWritten, false, null);

        String xmlIn = wrapInRoot(contentIn);
        // actual SAX parsing
        XmlTool.parseAndWriteXml(xmlIn, xml2string);
        String xmlOut = xml2string.resultString();

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
    @MethodSource("com.graphinout.foundation.xml.XmlTests#xmlStringsSaxParsed")
    void testParse(Input_Expected in_out) throws Exception {
        String inContent = in_out.input();
        String inXml = wrapInRoot(inContent);

        String expectedContent = in_out.expected();
        String expectedXml = wrapInRoot(expectedContent);


        Xml2StringWriter xml2string = new Xml2StringWriter(XML.AttributeOrderPerElement.AsWritten,
                // use no output encoding
                false, null);
        XmlTool.parseAndWriteXml(inXml, xml2string);
        String outActualXml = xml2string.resultString();
        TextTests.xAssertEqual(outActualXml, expectedXml);
    }

    @ParameterizedTest
    @MethodSource("com.graphinout.foundation.xml.XmlTests#xmlStringsSaxParsed")
    void testSimulatedSaxParse(Input_Expected in_out) throws Exception {
        String inContent = in_out.input();
        String expectedContent = in_out.expected();

        String actual = XmlTool.normaliseLikeEntityPreprocessingThenSaxParsing(inContent);
        TextTests.xAssertEqual(actual, expectedContent);
    }


}
