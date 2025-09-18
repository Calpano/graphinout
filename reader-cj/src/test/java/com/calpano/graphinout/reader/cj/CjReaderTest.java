package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.verifyNoInteractions;

//@Disabled
class CjReaderTest {

    public static final String EMPTY_FILE = "";
    private AutoCloseable closeable;
    private ConnectedJsonReader underTest;
    @Mock private GioWriter mockGioWriter;
    private List<ContentError> capturedErrors;
    private Consumer<ContentError> errorConsumer;

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream()
                .map(Resource::getPath).
                filter(ConnectedJsonReader.FORMAT::matches)
                .filter(path -> !path.contains("/extended/"));
    }

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
