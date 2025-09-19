package com.calpano.graphinout.foundation.json.json5;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
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

    private AutoCloseable closeable;
    private Json5Reader underTest;
    @Mock private GioWriter mockGioWriter;
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
    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenJson5IsEmpty() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-empty", "");

        underTest.errorHandler(errorConsumer);
        underTest.read(inputSource, mockGioWriter);

        verifyNoInteractions(mockGioWriter);
        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldParseExampleConnectedJson5File() throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource("json5/cj-extended-json5/example.connected.json5");
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of("example.connected.json5", content);

        underTest.errorHandler(errorConsumer);
        underTest.read(singleInputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));

        // Should create multiple nodes and edges based on the example file
        // We don't verify exact counts here, just that the structure is correct
        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

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

        underTest.errorHandler(errorConsumer);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));

        // Verify nodes are created
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        // Verify edge is created
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

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
