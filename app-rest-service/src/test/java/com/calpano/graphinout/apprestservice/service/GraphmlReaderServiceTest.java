package com.calpano.graphinout.apprestservice.service;

import static org.junit.jupiter.api.Assertions.*;

import com.calpano.graphinout.base.input.FilesMultiInputSource;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.filemanagment.IOResource;
import com.calpano.graphinout.filemanagment.LoadFile;
import com.calpano.graphinout.filemanagment.SaveFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import static org.mockito.BDDMockito.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // need this in Spring Boot test
class GraphmlReaderServiceTest {

  @TestConfiguration
  static class GraphmlReaderServiceTestConfiguration {
    @Bean
    public LoadFile loadFile() {
      return new MockLoadFile();
    }

    @Bean
    public SaveFile saveFile() {
      return new MockSaveFile();
    }
  }


  @Autowired GraphmlReaderService graphmlReaderService;



  @BeforeEach
  void setUp() {}

  @AfterEach
  void tearDown() {
      MockitoAnnotations.openMocks(this);
  }

  @Test
  void read() throws IOException {
    graphmlReaderService.read("123");
  }

  private static class MockLoadFile implements LoadFile {

    @Override
    public IOResource<InputSource> loadFile(@NotNull String inputId) throws IOException {
      FilesMultiInputSource dataAndMapping =
          new FilesMultiInputSource() //
              .withFile("data", new File("./src/test/resources/json-test/boardgames_40.json")) //
              .withFile(
                  "mapping", new File("./src/test/resources/json-test/json-mapper-1.json") //
                  );

      return new IOResource<InputSource>(dataAndMapping, "json");
    }
  }


  private static class MockSaveFile<OutputStream> implements SaveFile<OutputStream> {

    @Override
    public String saveFile(@NotNull IOResource<OutputStream> inputSource, @Nullable String inputId)
        throws IOException {
      if (inputSource.getResource() instanceof ByteArrayOutputStream data) {
        log.info(data.toString());
      }

      return inputId;
    }
  }
}
