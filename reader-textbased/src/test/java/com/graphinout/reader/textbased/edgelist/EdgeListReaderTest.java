package com.graphinout.reader.textbased.edgelist;

import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.reader.textbased.adjlist.EdgeListReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class EdgeListReaderTest {

    private final CjFactory cjFactory = new CjFactory();
    private AutoCloseable closeable;
    private EdgeListReader underTest;
    @Mock private ICjStream mockCjStream;
    private List<ContentError> capturedErrors;
    private Consumer<ContentError> errorConsumer;

    private static Stream<TestFileProvider.TestResource> edgeListResourcePaths() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".edgelist"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new EdgeListReader();
        this.capturedErrors = new ArrayList<>();
        this.errorConsumer = capturedErrors::add;

        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> cjFactory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> cjFactory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> cjFactory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> cjFactory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);

    }

    @ParameterizedTest
    @MethodSource("edgeListResourcePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockCjStream);

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

}
