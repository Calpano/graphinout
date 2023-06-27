package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphmlPathBuilderTest {


    @Test
    void paths() throws Exception {
        List<String> expected = new ArrayList<>();
        expected.add("$[?(@.id == 10)]['credit']['desiger']");
        expected.add("$[?(@.id == 10)]['recommendations']['fans_liked']");

        Path inputSource = Paths.get("src", "test", "resources", "json-mapper", "json-mapper-1.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            PathBuilder pathBuilder = new GraphmlJsonPath(singleInputSource);
            Set<PathBuilder.PaarValue<?>> results = pathBuilder.findLink(10);
            System.out.println(results);
            assertAll("",
                    () -> assertEquals("$[*]", pathBuilder.findAll().path),
                    () -> assertEquals("$[*]['id']['title']", pathBuilder.findAllId().path),
                    () -> assertEquals("$[?(@.id == 10)]", pathBuilder.findById(10).path),
                    () -> assertEquals("$[?(@.id == '10')]", pathBuilder.findById("10").path),
                    () -> assertEquals("$[?(@.title == 'test')]", pathBuilder.findByLabel("test").path),
                    () -> assertEquals(2, results.size()),
                    () -> assertInstanceOf(PathBuilder.PaarValue.class, results.stream().toList().get(0)),
                    () -> assertInstanceOf(PathBuilder.PaarValue.class, results.stream().toList().get(1)),

                    () -> assertEquals("designed by", ((PathBuilder.Inline) results.stream().toList().get(1).path).edgeAttribute.name),
                    () -> assertEquals("$[?(@.id == 10)]['credit']['desiger']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.path),

                    () -> assertEquals("credit.desiger", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.name),
                    () -> assertEquals("$[?(@.id == 10)]['credit']['desiger']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.path),

                    () -> assertEquals("name", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeAttribute.name),
                    () -> assertEquals("$[?(@.id == 10)]['name']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeAttribute.path),

                    () -> assertEquals("id", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeId.name),
                    () -> assertEquals("$[?(@.id == 10)]['id']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeId.path));

        }
    }
}