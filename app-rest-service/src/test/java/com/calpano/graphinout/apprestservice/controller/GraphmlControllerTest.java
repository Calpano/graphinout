package com.calpano.graphinout.apprestservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.calpano.graphinout.apprestservice.service.GraphmlReaderService;
import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(GraphmlController.class)
@Disabled("TODO impl")
class GraphmlControllerTest {

  private static final File DATA_FILE =
      new File("./src/test/resources/json-test/boardgames_40.json");
  private static final File MAPPER_FILE =
      new File("./src/test/resources/json-test/json-mapper-1.json");
  @Autowired private MockMvc mockMvc;
  @MockBean private GraphmlReaderService service;

  @Test
  void statusTest() throws Exception {

    MediaType MEDIA_TYPE_TEXT_UTF8 =
        new MediaType("text", "plain", java.nio.charset.Charset.forName("UTF-8"));
    MockHttpServletRequestBuilder request = get("/api/status");
    request.content("".getBytes());

    request.accept(MEDIA_TYPE_TEXT_UTF8);
    request.contentType(MEDIA_TYPE_TEXT_UTF8);
    this.mockMvc
        .perform(request)
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("OK")));
  }

  @Test
  void readTest() {}

  @Test
  void validateTest() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "data-file",
            "graphml.json",
            MediaType.APPLICATION_JSON_VALUE,
            new FileInputStream(DATA_FILE));
    MockMultipartFile mapper =
        new MockMultipartFile(
            "format-file",
            "mapper.json",
            MediaType.APPLICATION_JSON_VALUE,
            new FileInputStream(MAPPER_FILE));

    MediaType MEDIA_TYPE_FILE_UTF8 =
        new MediaType("multipart", "form-data", java.nio.charset.Charset.forName("UTF-8"));
    MockHttpServletRequestBuilder request = multipart("/api/validate/json").file(file).file(mapper);

    request.accept(MEDIA_TYPE_FILE_UTF8);
    request.contentType(MEDIA_TYPE_FILE_UTF8);

    mockMvc.perform(request).andDo(print()).andExpect(status().isOk());
  }
}
