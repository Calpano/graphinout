package com.calpano.graphinout.reader.adjlist;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AdjListReaderTest {

    public static final String EMPTY_FILE = "";
    public static final String NODES_ONLY = "aaa\nbbb";
    public static final String SMALL = "aaa bbb ccc\nddd aaa";
    private AutoCloseable closeable;
    private AdjListReader underTest;
    @Mock private GioWriter mockGioWriter;
    private List<ContentError> capturedErrors;
    private Consumer<ContentError> errorConsumer;

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(path -> path.endsWith(".adjlist"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new AdjListReader();
        this.capturedErrors = new ArrayList<>();
        this.errorConsumer = capturedErrors::add;
    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenTGFIsEmpty() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-empty", EMPTY_FILE);

        underTest.errorHandler(errorConsumer);
        underTest.read(inputSource, mockGioWriter);

        verifyNoInteractions(mockGioWriter);
        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-nodes-only", NODES_ONLY);

        underTest.errorHandler(errorConsumer);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldWork() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-small", SMALL);

        underTest.errorHandler(errorConsumer);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        // aaa
        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id("aaa").build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        // bbb
        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id("bbb").build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        // aaa -> bbb
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        // ccc
        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id("ccc").build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        // aaa -> ccc
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        // ddd
        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id("ddd").build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        // ddd -> aaa
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

        verifyNoMoreInteractions(mockGioWriter);
    }

    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockGioWriter);

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

}
