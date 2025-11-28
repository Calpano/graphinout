package com.graphinout.reader.gml;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.json.writer.impl.Json2StringWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

class GmlReaderTest {

    public static final String GML_EXAMPLE = """
            Creator "yFiles" Version 2.2 graph [ hierarchic 1 directed 1
            node [ id 0 graphics [ x 200.0 y 0.0 ] LabelGraphics [ text "January" ] ]
            node [ id 1 graphics [ x 425.0 y 75.0 ] LabelGraphics [ text "December" ] ]
            edge [ source 1 target 0 graphics [ Line [ point [ x 425.0 y 75.0 ] point [ x 425.0 y 0.0 ] point [ x 200.0 y 0.0 ] ] ] LabelGraphics [ text "Happy New Year!" model "six_pos" position "head" ] ] ]""";
    private static final Logger log = getLogger(GmlReaderTest.class);
    private AutoCloseable closeable;
    private GmlReader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;
    @Mock private Consumer<ContentError> mockErrorConsumer;

    public static Stream<TestFileProvider.TestResource> gmlResources() {
        return TestFileProvider.getAllTestResources() //
                .filter(res -> res.resource().getPath().endsWith(GmlReader.FORMAT.mainExtension()));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    /**
     * Parse GML to CJ and compared with expected CJ.
     *
     * @param path
     * @throws IOException
     */
    @ParameterizedTest
    @ValueSource(strings = {"text/gml/example-small.gml", "text/gml/array.gml"})
    void parseGmlToExpectedCj(String path) throws IOException {
        Resource resource = TestFileUtil.resource(path);
        Assertions.assertNotNull(resource);
        String displayPath = resource.getPath();
        if (TestFileUtil.isInvalid(resource, "gml")) {
            log.info("Skipping invalid GML file " + resource.getURI());
            return;
        }

        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        CjWriter2CjDocumentWriter cjWriter2cjDoc = new CjWriter2CjDocumentWriter();
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cjWriter2cjDoc);
        underTest.read(singleInputSource, cjStream2CjWriter);

        ICjDocument actualCjDoc = cjWriter2cjDoc.resultDoc();
        Resource expected = TestFileUtil.withAnotherExtension(resource,".gml", ".cj.json");
        Assertions.assertNotNull(expected);
        ICjDocument expectedCjDoc = CjDocuments.parseCjJsonString(expected.getPath(), expected.getContentAsString());

        CjAssert.xAssertThatIsSameCj(actualCjDoc, expectedCjDoc, () -> {
            log.info("GML tokens: " + GmlTokenizer.tokenizeToList(content));
        });
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new GmlReader();

        // The reader requests chunk instances and a JSON factory from the stream; stub the mock accordingly
        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldParseGmlExample() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(GML_EXAMPLE.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.setContentErrorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        // Verify interactions counts with adapter semantics
        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(1)).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).graphEnd();
        verify(mockCjStream, times(1)).documentEnd();
    }

    @ParameterizedTest
    @MethodSource("gmlResources")
    void shouldWorkAsIntended(String displayPath, Resource resource) throws IOException {
        if (TestFileUtil.isInvalid(resource, "gml")) {
            log.info("Skipping invalid GML file " + resource.getURI());
            return;
        }

        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter);
        underTest.read(singleInputSource, cjStream2CjWriter);
        String json = json2StringWriter.jsonString();
        log.info("JSON: " + json);
    }

    @Test
    void testResources() {
        assertThat(GmlReader.FORMAT.mainExtension()).isEqualTo(".gml");
        assertThat(TestFileProvider.getAllTestResources().findAny().isPresent()).isTrue();
        assertThat(gmlResources().findAny().isPresent()).isTrue();
    }

}
