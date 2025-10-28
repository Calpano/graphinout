package com.graphinout.foundation.json.json5;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.stream.api.ICjFactory;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;

class Json5ReaderTest {

    private final ICjFactory cjFactory = new CjFactory();
    private AutoCloseable closeable;
    private Json5Reader underTest;
    @Mock private ICjStream mockCjStream;
    private List<ContentError> capturedErrors;
    private Consumer<ContentError> errorConsumer;

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new Json5Reader();
        this.capturedErrors = new ArrayList<>();
        this.errorConsumer = capturedErrors::add;

        // setup chunk methods
        Mockito.when(mockCjStream.createDocumentChunk()).thenReturn(cjFactory.createDocumentChunk());
        Mockito.when(mockCjStream.createGraphChunk()).thenReturn(cjFactory.createGraphChunk());
        Mockito.when(mockCjStream.createNodeChunk()).thenReturn(cjFactory.createNodeChunk());
        Mockito.when(mockCjStream.createNodeChunkWithId(any(String.class))).thenAnswer(invocation -> cjFactory.createNodeChunkWithId(invocation.getArgument(0)));
        Mockito.when(mockCjStream.createEdgeChunk()).thenReturn(cjFactory.createEdgeChunk());
    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenJson5IsEmpty() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-empty", "");

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        verifyNoInteractions(mockCjStream);
        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldParseExampleConnectedJson5File() throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource("json5/cj-extended-json5/example.connected.json5");
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of("example.connected.json5", content);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(singleInputSource, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        inOrder.verify(mockCjStream).graphStart(any(ICjGraphChunk.class));

        // Should create multiple nodes and edges based on the example file
        // We don't verify exact counts here, just that the structure is correct
        inOrder.verify(mockCjStream).graphEnd();
        inOrder.verify(mockCjStream).documentEnd();

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldParseJson5WithComments() throws IOException {
        String json5Content = """
                {
                  "nodes": [
                    { "id": "a" },
                    { "id": "b" }
                  ],
                  // This is a comment
                  "edges": [
                    { "source": "a", "target": "b" }
                  ]
                }
                """;

        SingleInputSource inputSource = SingleInputSource.of("test-json5", json5Content);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        inOrder.verify(mockCjStream).graphStart(any(ICjGraphChunk.class));

        // Verify nodes are created
        inOrder.verify(mockCjStream).node(any(ICjNodeChunk.class));
        inOrder.verify(mockCjStream).node(any(ICjNodeChunk.class));

        // Verify edge is created
        inOrder.verify(mockCjStream).edge(any(ICjEdgeChunk.class));

        inOrder.verify(mockCjStream).graphEnd();
        inOrder.verify(mockCjStream).documentEnd();

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldReturnCorrectFileFormat() {
        var fileFormat = underTest.fileFormat();
        assert fileFormat.id().equals("json5");
        assert fileFormat.label().equals("Connected JSON5 Format");
        assert fileFormat.fileExtensions().contains(".json5");
    }

}
