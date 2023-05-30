package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

class JsonPathTest {


    @Test
    void paths() throws Exception {
        List<String> expected= new ArrayList<>();
        expected.add("$.id.credit.desiger");
        expected.add("$.id.recommendations.fans_liked");

        Path inputSource = Paths.get("src", "test", "resources", "json-mapper", "json-mapper-1.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            JsonPath jsonPath = new JsonPath(singleInputSource);
            Set<String> results= jsonPath.paths();
            assertAll("",
                    ()->assertEquals(2,results.size()),
                    () ->assertTrue(results.containsAll(expected))
            );
        }
    }
}