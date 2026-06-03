package com.graphinout.reader.ddot;

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

class DDotReaderTest {

    public static final String EMPTY_FILE = "";
    public static final String SIMPLE = """
            Alice .. knows .. Bob
            Bob .. knows .. Carol""";
    public static final String CONTINUATION = """
            Alice .. knows .. Bob
            .. likes .. Carol
            .. hates .. Dave""";
    public static final String COMMENTS_AND_BLANKS = """
            # a comment
            Alice .. knows .. Bob

            # another
            Carol .. knows .. Dave""";
    public static final String ON_OFF = """
            Alice .. knows .. Bob
            ddot.it/off
            Should .. not .. emit
            ddot.it/on
            Carol .. knows .. Dave""";

    private AutoCloseable closeable;
    private DDotReader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;

    private static Stream<TestFileProvider.TestResource> ddotResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".ddot"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new DDotReader();

        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldEmitDocOnlyWhenEmpty() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream).createDocumentChunk();
        verify(mockCjStream).document(any(ICjDocumentChunk.class));
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).level).isEqualTo(ContentError.ErrorLevel.Warn);
    }

    @Test
    void shouldReadSimpleTriples() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(SIMPLE.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(1)).graphStart(any(ICjGraphChunk.class));
        // Alice, Bob, Carol = 3 unique nodes
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).graphEnd();
        verify(mockCjStream, times(1)).documentEnd();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldSupportContinuationLines() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(CONTINUATION.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        // Alice, Bob, Carol, Dave = 4 unique nodes
        verify(mockCjStream, times(4)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldSkipCommentsAndBlanks() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(COMMENTS_AND_BLANKS.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream, times(4)).node(any(ICjNodeChunk.class)); // Alice, Bob, Carol, Dave
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldHonorOnOffSwitches() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(ON_OFF.getBytes(StandardCharsets.UTF_8)));

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(mockInputSrc, mockCjStream);

        // Only Alice/Bob and Carol/Dave should be emitted; the disabled triple is ignored
        verify(mockCjStream, times(4)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ddotResources")
    void shouldParseAllTestResources(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(singleInputSource, mockCjStream);

        // No higher-than-Warn issues
        long fatal = errors.stream().filter(ContentError::isError).count();
        assertThat(fatal).isEqualTo(0L);
    }

    @Test
    void testProviderHasResources() {
        assertThat(ddotResources().toList()).isNotEmpty();
    }
}
