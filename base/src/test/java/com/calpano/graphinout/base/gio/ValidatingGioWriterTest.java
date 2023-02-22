package com.calpano.graphinout.base.gio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URL;

class ValidatingGioWriterTest {
    private AutoCloseable closeable;
    private ValidatingGioWriter underTest;
    @Mock
    private GioWriterImpl mockGioWriterImpl;
    @Mock
    private GioKey mockKey;
    @Mock
    private GioDocument mockDocument;
    @Mock
    private GioGraph mockGraph;
    @Mock
    private GioEdge mockEdge;
    @Mock
    private GioNode mockNode;
    @Mock
    private URL mockLocator;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new ValidatingGioWriter(mockGioWriterImpl);

    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}