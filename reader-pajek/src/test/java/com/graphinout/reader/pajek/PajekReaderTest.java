package com.graphinout.reader.pajek;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PajekReaderTest {

    private static final String DIRECTED_ARCS = """
            *Vertices 3
            1 "Alice"
            2 "Bob"
            3 "Carol"
            *Arcs
            1 2
            2 3
            """;

    private static final String UNDIRECTED_EDGES = """
            *Vertices 2
            1 "Alice"
            2 "Bob"
            *Edges
            1 2
            """;

    private static final String MIXED_ARCS_AND_EDGES = """
            *Vertices 3
            1 "Alice"
            2 "Bob"
            3 "Carol"
            *Arcs
            1 2
            *Edges
            2 3
            """;

    private static final String WITH_COMMENTS = """
            % This is a comment
            *Vertices 2
            % Another comment
            1 "Alice"
            2 "Bob"
            *Edges
            1 2
            """;

    private AutoCloseable closeable;
    private PajekReader underTest;
    @Mock private ICjStream mockCjStream;

    private static Stream<TestFileProvider.TestResource> pajekResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".net"));
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        underTest = new PajekReader();

        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void shouldEmitEmptyDocumentForBlankContent() throws IOException {
        SingleInputSource src = SingleInputSource.of("empty.net", "");

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        verify(mockCjStream).createDocumentChunk();
        verify(mockCjStream).document(any(ICjDocumentChunk.class));
        verifyNoMoreInteractions(mockCjStream);
    }

    @Test
    void shouldParseDirectedArcs() throws IOException {
        SingleInputSource src = SingleInputSource.of("arcs.net", DIRECTED_ARCS);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).nodeEnd();
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseUndirectedEdges() throws IOException {
        SingleInputSource src = SingleInputSource.of("edges.net", UNDIRECTED_EDGES);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldParseMixedArcsAndEdges() throws IOException {
        SingleInputSource src = SingleInputSource.of("mixed.net", MIXED_ARCS_AND_EDGES);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        verify(mockCjStream, times(3)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldIgnoreCommentLines() throws IOException {
        SingleInputSource src = SingleInputSource.of("comments.net", WITH_COMMENTS);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("pajekResources")
    void shouldParseWithoutErrors(String displayPath, Resource resource) throws IOException {
        String content = resource.getContentAsString();
        SingleInputSource src = SingleInputSource.of(displayPath, content);

        List<ContentError> errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);
        underTest.read(src, mockCjStream);

        assertThat(errors).isEmpty();
    }

    @Test
    void testProvider() {
        assertThat(pajekResources().toList()).isNotEmpty();
    }
}
