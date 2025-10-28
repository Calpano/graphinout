package com.graphinout.reader.graphml;

import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.base.graphml.GraphmlWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.Xml2StringWriter;
import com.graphinout.foundation.xml.XmlNormalizer;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.CjStream2GraphmlWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlXmlNamespacesTest {

    public static final String TEST_ID = "GraphmlNS";

    private static Stream<TestFileProvider.TestResource> namespaceResources() {
        return TestFileProvider.graphmlResources().filter(tr -> tr.resource().getPath().contains("synthetic/namespace"));
    }

    /**
     * Parse XML->Graphml->CJ->Graphml->XML
     *
     * @param resource
     * @return
     * @throws IOException
     */
    private static String parse_uri_string_graphml_xml_string(Resource resource) throws IOException {
        GraphmlReader graphmlReader = new GraphmlReader();
        SingleInputSource singleInputSource = inputSource(resource);
        @Nullable ICjDocument cjDoc = graphmlReader.readToCjDocument(singleInputSource);

        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        CjDocument2Graphml.writeToGraphml(cjDoc, graphml2XmlWriter);
        return xmlWriter.resultString();
    }

    @Test
    void namespace2() throws IOException {
        Resource resource = TestFileUtil.resource("xml/graphml/synthetic/namespace/namespace2.graphml.xml");
        assertThat(resource).isNotNull();
        String expected = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:foo="http://foo.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">
                </graphml>"""; //
        String actual = parse_uri_string_graphml_xml_string(resource);
        assertEquals(expected, actual);
    }

    @Test
    void namespace3() throws IOException {
        Resource resource = TestFileUtil.resource("xml/namespace3.xml");
        assertThat(resource).isNotNull();
        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = inputSource(resource);
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GraphmlWriter graphmlWriter = new Graphml2XmlWriter(xmlWriter);
        CjStream2GraphmlWriter cjStream2GraphmlWriter = new CjStream2GraphmlWriter(graphmlWriter);

        List<ContentError> errorList = new ArrayList<>();
        GraphmlReader graphmlReader = new GraphmlReader();
        graphmlReader.setContentErrorHandler(errorList::add);
        graphmlReader.read(singleInputSource, cjStream2GraphmlWriter);
        assertEquals(2, errorList.size(), "Expect contentError for <myRoot> element");
        ContentError first = errorList.getFirst();
        assertEquals(ContentError.ErrorLevel.Error, first.getLevel());
        assertEquals("While parsing 2:83\n" +
                "Message: XML Element <myroot> is not a Graphml tag and not allowing XML here. XmlParseContext{elementStack=[], mode=Graphml}", first.getMessage());
    }

    @Test
    void testResourceLoading() {
        assertThat(namespaceResources()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("namespaceResources")
    void xmlNamespaceTest(String displayPath, Resource resource) throws IOException {
        @Nullable Resource expectedResource = TestFileUtil.expectedResource(resource, TEST_ID);
        assertThat(expectedResource).isNotNull();

        String actual = parse_uri_string_graphml_xml_string(resource);
        actual = XmlNormalizer.normalize(actual);

        String expected = expectedResource.getContentAsString();
        expected = XmlNormalizer.normalize(expected);

        assertEquals(expected, actual);
    }

}
