package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

class GraphmlPathBuilderTest {

    private static final Logger log = getLogger(GraphmlPathBuilderTest.class);

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
            Set<PathBuilder.PairValue<?>> results = pathBuilder.findLink(10);
            System.out.println(results);
            assertAll("", () -> assertEquals("$[*]", pathBuilder.findAll().path), () -> assertEquals("$[*]['id']['title']", pathBuilder.findAllId().path), () -> assertEquals("$[?(@.id == 10)]", pathBuilder.findById(10).path), () -> assertEquals("$[?(@.id == '10')]", pathBuilder.findById("10").path), () -> assertEquals("$[?(@.title == 'test')]", pathBuilder.findByLabel("test").path), () -> assertEquals(2, results.size()), () -> assertInstanceOf(PathBuilder.PairValue.class, results.stream().toList().get(0)), () -> assertInstanceOf(PathBuilder.PairValue.class, results.stream().toList().get(1)),

                    () -> assertEquals("designed by", ((PathBuilder.Inline) results.stream().toList().get(1).path).edgeAttribute.name), () -> assertEquals("$[?(@.id == 10)]['credit']['desiger']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.path),

                    () -> assertEquals("credit.desiger", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.name), () -> assertEquals("$[?(@.id == 10)]['credit']['desiger']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodes.path),

                    () -> assertEquals("name", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeAttribute.name), () -> assertEquals("$[?(@.id == 10)]['name']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeAttribute.path),

                    () -> assertEquals("id", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeId.name), () -> assertEquals("$[?(@.id == 10)]['id']", ((PathBuilder.Inline) results.stream().toList().get(1).path).targetNodeId.path));

        }
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        List<Links.Link> links = new ObjectMapper().readValue(json, new TypeReference<List<Links.Link>>(){});
        assertNotNull(links);

        String result = new ObjectMapper().writeValueAsString(links);
        log.info("res={}", result);
    }

  final String json = "[{\"refer\":\"existing\",\"linkLabel\":\"recommended\",\"idTarget\":\"recommendations.fans_liked\"},{\"refer\":\"inline\",\"linkLabel\":\"designed by\",\"target\":\"credit.designer\",\"id\":\"id\",\"label\":\"name\"}]";


    @Test
    void testSerialize() throws JsonProcessingException {
        List<Links.Link> links = new ArrayList<>();
        Links.LinkToExistingNode link1 = Links.LinkToExistingNode.of("recommended", "recommendations.fans_liked");
        links.add(link1);
        links.add(Links.LinkCreateNode.of("designed by", "credit.designer", "id", "name"));

        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Links.Link.class);
        String result = mapper.writer().forType(listType).writeValueAsString(links);
        log.info("res={}", result);
        assertEquals(json, result);
    }
}
