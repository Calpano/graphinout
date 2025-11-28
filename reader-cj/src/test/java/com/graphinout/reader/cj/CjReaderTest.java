package com.graphinout.reader.cj;

import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.factory.ICjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CjReaderTest {

    public static final String EMPTY_FILE = "";
    private AutoCloseable closeable;
    private ConnectedJsonReader underTest;
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
        this.underTest = new ConnectedJsonReader();
        this.capturedErrors = new ArrayList<>();
        this.errorConsumer = capturedErrors::add;

        // chunk creation
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> cjFactory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> cjFactory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> cjFactory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> cjFactory.createEdgeChunk());
    }

    private final ICjFactory cjFactory = new CjFactory();

    @Disabled("Handling empty files is underspecified")
    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenFileIsEmpty() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-empty", EMPTY_FILE);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        verifyNoInteractions(mockCjStream);
        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }


    @ParameterizedTest
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    void shouldWorkAsIntended(String displayName, Resource resource) throws IOException {
        SingleInputSource singleInputSource = inputSource(resource);

        underTest.read(singleInputSource, mockCjStream);

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

}
