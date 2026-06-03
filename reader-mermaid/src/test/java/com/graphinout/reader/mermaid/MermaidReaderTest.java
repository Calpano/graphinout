package com.graphinout.reader.mermaid;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MermaidReaderTest {

    public static final String FLOWCHART_SIMPLE = """
            flowchart LR
                A --> B
                B --> C""";
    public static final String FLOWCHART_WITH_LABELS = """
            flowchart TD
                A[Start] --> B(Middle)
                B -->|next| C[End]""";
    public static final String CLASS_DIAGRAM = """
            classDiagram
                class Animal
                class Dog
                Animal <|-- Dog""";
    public static final String STATE_DIAGRAM = """
            stateDiagram-v2
                [*] --> S1
                S1 --> S2 : event
                S2 --> [*]""";
    public static final String SANKEY = """
            sankey-beta
            A,B,5
            B,C,10""";

    private AutoCloseable closeable;
    private MermaidReader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;

    private static Stream<TestFileProvider.TestResource> mermaidResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".mmd"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new MermaidReader();

        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldEmitDocOnlyWhenEmpty() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream).createDocumentChunk();
        verify(mockCjStream).document(any(ICjDocumentChunk.class));
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldParseSimpleFlowchart() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(FLOWCHART_SIMPLE.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class)); // A, B, C
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseFlowchartWithLabels() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(FLOWCHART_WITH_LABELS.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseClassDiagram() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(CLASS_DIAGRAM.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(2)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseStateDiagram() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(STATE_DIAGRAM.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        // S1, S2, [*]→__start_end__ → 3 nodes
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseSankey() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(SANKEY.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class)); // A, B, C
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("mermaidResources")
    void shouldParseAllTestResources(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource sis = SingleInputSource.of(displayPath, content);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(sis, mockCjStream);

        long fatal = errors.stream().filter(ContentError::isError).count();
        assertThat(fatal).isEqualTo(0L);
        // At least documentStart was called for non-empty content
        verify(mockCjStream, atLeast(1)).createDocumentChunk();
    }

    @Test
    void testProviderHasResources() {
        assertThat(mermaidResources().toList()).isNotEmpty();
    }
}
