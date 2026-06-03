package com.graphinout.reader.d2;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class D2ReaderTest {

    public static final String SIMPLE = """
            a -> b
            b -> c""";
    public static final String WITH_LABELS = """
            alice: Alice
            bob: Bob
            alice -> bob: greets""";
    public static final String CONTAINER = """
            cloud: {
              server1
              server2
            }
            cloud.server1 -> cloud.server2""";
    public static final String CONNECTIONS = """
            a -> b
            a <- b
            a <-> b
            a -- b""";
    public static final String COMMENTS =
            "# comment\n" +
            "a -> b\n" +
            "\"\"\" block comment \"\"\"\n" +
            "b -> c";

    private AutoCloseable closeable;
    private D2Reader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;

    private static Stream<TestFileProvider.TestResource> d2Resources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".d2"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new D2Reader();

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
    void shouldParseSimpleConnections() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(SIMPLE.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class)); // a, b, c
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseLabels() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(WITH_LABELS.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(2)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseContainerWithDottedRefs() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(CONTAINER.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        // cloud, cloud.server1, cloud.server2 — 3 unique nodes
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseAllConnectionVariants() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(CONNECTIONS.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(2)).node(any(ICjNodeChunk.class)); // a, b
        // 4 statements: ->, <-, <->, -- ; <-> produces 2 edges so total = 5
        verify(mockCjStream, times(5)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldSkipComments() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(COMMENTS.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class)); // a, b, c
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("d2Resources")
    void shouldParseAllTestResources(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource sis = SingleInputSource.of(displayPath, content);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(sis, mockCjStream);

        long fatal = errors.stream().filter(ContentError::isError).count();
        assertThat(fatal).isEqualTo(0L);
    }

    @Test
    void testProviderHasResources() {
        assertThat(d2Resources().toList()).isNotEmpty();
    }
}
