package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlSAXSchemaValidationTest {

    private static final Logger log = getLogger(GraphmlReaderTest2.class);

    private static Stream<String> getAllGraphmlFiles() {
        return ReaderTests.getAllTestResourceFilePaths().filter(path -> path.endsWith(".graphml"));
    }

    @BeforeEach
    void setUp() {
    }

    protected Map<String, Long> expectedErrors(@NotNull String resourceName) {
        Map<String, Long> errorLongMap = new HashMap<>();
        switch (resourceName) {


        }
        return errorLongMap;
    }

    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws Exception {

        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            boolean isValid = graphmlReader.isValid(singleInputSource);
            Map<String, Long> actualErrors = contentErrors.stream().collect(Collectors.groupingBy(ContentError::toString, Collectors.counting()));
            Map<String, Long> expectedErrors = expectedErrors(filePath);
            print(contentErrors,filePath);
            for (Map.Entry<String, Long> entry : actualErrors.entrySet()) {
                assertFalse(isValid);
                assertTrue(expectedErrors.containsKey(entry.getKey()));
                assertEquals(expectedErrors.get(entry.getKey()), entry.getValue());
            }
            // check that each expected error actually happened
//            for (Map.Entry<String, Long> entry : expectedErrors.entrySet()) {
//                assertTrue(actualErrors.containsKey(entry.getKey()),"Expected error "+entry.getKey());
//                assertEquals(actualErrors.get(entry.getKey()), entry.getValue());
//            }
        }
    }
      private void print( List<ContentError> contentErrors, String  file) throws IOException {

        if(contentErrors.isEmpty()) return;
          FileWriter fileWriter = new FileWriter("./out.txt",true);
          PrintWriter printWriter = new PrintWriter(fileWriter);

          printWriter.printf("case \"%s\":\n",file);
          Map<String, Long> actualErrors = contentErrors.stream().collect(Collectors.groupingBy(ContentError::toString, Collectors.counting()));
          for(ContentError c : contentErrors){
            printWriter.printf("errorLongMap.put(new ContentError(ContentError.ErrorLevel.%s, \"%s\", null).toString(), %dL);\n",c.getLevel(),c.getMessage(),actualErrors.get(c.toString()));
        }
          printWriter.print("return errorLongMap;\n");
          printWriter.close();
      }

    @Test
    void read() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "aws", "AWS - Analytics.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            boolean isValid = graphmlReader.isValid(singleInputSource);
            Map<String, Long> expectedErrors = expectedErrors("graphin/graphml/aws/AWS - Analytics.graphml");

            Map<String, Long> map = contentErrors.stream().collect(Collectors.groupingBy(ContentError::toString, Collectors.counting()));
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                assertFalse(isValid);
                assertTrue(expectedErrors.containsKey(entry.getKey()));
                assertEquals(expectedErrors.get(entry.getKey()), entry.getValue());
//
            }
        }
    }
}
