package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.TestFileProvider;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlNormalizer;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.TestFileUtil.inputSource;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlXmlNamespacesTest {

    public static final String TEST_ID = "GraphmlNS";

    private static Stream<TestFileProvider.TestResource> namespaceResources() {
        return TestFileProvider.graphmlResources().filter(tr -> tr.resource().getPath().contains("synthetic/namespace"));
    }

    private static String parse_uri_string_graphml_gio_xml_string(Resource resource) throws IOException {
        SingleInputSource singleInputSource = inputSource(resource);
        GraphmlReader graphmlReader = new GraphmlReader();
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(xmlWriter));
        graphmlReader.read(singleInputSource, gioWriter);
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
        String actual = parse_uri_string_graphml_gio_xml_string(resource);
        assertEquals(expected, actual);
    }

    @Test
    void namespace3() throws IOException {
        Resource resource = TestFileUtil.resource("xml/namespace3.xml");
        assertThat(resource).isNotNull();
        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = inputSource(resource);
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(xmlWriter));
        List<ContentError> errorList = new ArrayList<>();
        graphmlReader.errorHandler(errorList::add);
        graphmlReader.read(singleInputSource, gioWriter);
        assertEquals(2, errorList.size(), "Expect contentError for <myRoot> element");
        assertEquals(ContentError.ErrorLevel.Warn, errorList.getFirst().getLevel());
        assertEquals("The Element <myroot> not acceptable tag for Graphml.", errorList.getFirst().getMessage());
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

        String actual = parse_uri_string_graphml_gio_xml_string(resource);
        actual = XmlNormalizer.normalize(actual);

        String expected = expectedResource.getContentAsString();
        expected = XmlNormalizer.normalize(expected);

        assertEquals(expected, actual);
    }

}
