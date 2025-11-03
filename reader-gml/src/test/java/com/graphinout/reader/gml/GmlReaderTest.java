package com.graphinout.reader.gml;

import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import io.github.classgraph.Resource;
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
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GmlReaderTest {

    public static final String GML_EXAMPLE = """
            Creator "yFiles" Version 2.2 graph [ hierarchic 1 directed 1 node [ id 0 graphics [ x 200.0 y 0.0 ] LabelGraphics [ text "January" ] ] node [ id 1 graphics [ x 425.0 y 75.0 ] LabelGraphics [ text "December" ] ] edge [ source 1 target 0 graphics [ Line [ point [ x 425.0 y 75.0 ] point [ x 425.0 y 0.0 ] point [ x 200.0 y 0.0 ] ] ] LabelGraphics [ text "Happy New Year!" model "six_pos" position "head" ] ] ]""";
    private AutoCloseable closeable;
    private GmlReader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;
    @Mock private Consumer<ContentError> mockErrorConsumer;

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new GmlReader();

        // The reader requests chunk instances and a JSON factory from the stream; stub the mock accordingly
        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldParseGmlExample() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(GML_EXAMPLE.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.setContentErrorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        // Verify interactions counts with adapter semantics
        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(1)).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(1)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).graphEnd();
        verify(mockCjStream, times(1)).documentEnd();
    }

}
