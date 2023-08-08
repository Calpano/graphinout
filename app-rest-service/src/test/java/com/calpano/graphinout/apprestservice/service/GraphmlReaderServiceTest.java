package com.calpano.graphinout.apprestservice.service;

import com.calpano.graphinout.apprestservice.utility.FileManager;
import com.calpano.graphinout.base.input.FilesMultiInputSource;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadInputSource;
import com.calpano.graphinout.filemanagment.SaveOutputSink;
import com.calpano.graphinout.filemanagment.Type;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j

@SpringBootTest
class GraphmlReaderServiceTest {

  @Autowired GraphmlReaderService graphmlReaderService;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void read() throws IOException {
    graphmlReaderService.read("123");
  }

  @TestConfiguration
  static class GraphmlReaderServiceTestConfiguration {

    @Bean
    public LoadInputSource loadFile() {
      return new MockLoadInputSource();
    }

    @Bean
    public SaveOutputSink saveOutputSink() {
      return new MockSaveOutputSink();
    }
  }

  private static class MockLoadInputSource implements LoadInputSource {

    @Override
    public IOResource<InputSource> load(@NotNull String sessionID, Type type) throws IOException {
      FilesMultiInputSource dataAndMapping =
          new FilesMultiInputSource() //
              // TODO use constants for 'data' and 'mapping'
              .withFile("data", new File("./src/test/resources/json-test/boardgames_40.json")) //
              .withFile(
                  "mapping", new File("./src/test/resources/json-test/json-mapper-1.json") //
                  );
      return new IOResource<InputSource>(dataAndMapping, type);
    }
  }

  private static class MockSaveOutputSink implements SaveOutputSink {

    @Override
    public String save(
        @NotNull IOResource<OutputSink> outputSink, @NotNull String sessionId, Type type)
        throws IOException {
      if (outputSink.getResource() instanceof InMemoryOutputSink data) {
        log.info(data.toString());
      }

      return sessionId;
    }
  }
}
