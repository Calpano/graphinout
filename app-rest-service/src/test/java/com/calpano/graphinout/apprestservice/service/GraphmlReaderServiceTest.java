package com.calpano.graphinout.apprestservice.service;

import com.calpano.graphinout.base.input.FilesMultiInputSource;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadInputSource;
import com.calpano.graphinout.filemanagment.SaveOutputSink;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // need this in Spring Boot test
class GraphmlReaderServiceTest {

    @TestConfiguration
    static class GraphmlReaderServiceTestConfiguration {
        @Bean
        public LoadInputSource loadFile() {
            return new MockLoadInputSource();
        }

        @Bean
        public SaveOutputSink saveFile() {
            return new MockSaveOutputSink();
        }
    }

    private static class MockLoadInputSource implements LoadInputSource {

        @Override
        public IOResource<InputSource> load(@NotNull String inputId) throws IOException {
            FilesMultiInputSource dataAndMapping = new FilesMultiInputSource() //
                    // TODO use constants for 'data' and 'mapping'
                    .withFile("data", new File("./src/test/resources/json-test/boardgames_40.json")) //
                    .withFile("mapping", new File("./src/test/resources/json-test/json-mapper-1.json") //
                    );

            return new IOResource<InputSource>(dataAndMapping, "json");
        }
    }

    private static class MockSaveOutputSink implements SaveOutputSink {

        @Override
        public String save(@NotNull IOResource<OutputSink> outputSink, @Nullable String inputId) throws IOException {
            if (outputSink.getResource() instanceof ByteArrayOutputStream data) {
                log.info(data.toString());
            }

            return inputId;
        }
    }

    @Autowired
    GraphmlReaderService graphmlReaderService;

    @Test
    void read() throws IOException {
        graphmlReaderService.read("123");
        // TODO verify result
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        MockitoAnnotations.openMocks(this);
    }
}
