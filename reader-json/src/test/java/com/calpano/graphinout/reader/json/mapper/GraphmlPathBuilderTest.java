package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonpath.JsonPath.using;
import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

class GraphmlPathBuilderTest {

    private static final Logger log = getLogger(GraphmlPathBuilderTest.class);
    final String json = "[{\"refer\":\"existing\",\"linkLabel\":\"recommended\",\"idTarget\":\"recommendations.fans_liked\"},{\"refer\":\"inline\",\"linkLabel\":\"designed by\",\"target\":\"credit.designer\",\"id\":\"id\",\"label\":\"name\"}]";

    @Test
    void loadObjectFromMapperFile() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "json-mapper", "json-mapper-1.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            GraphmlJsonMapperLoader graphmlJsonMapperLoader = new GraphmlJsonMapperLoader(singleInputSource);
            GraphmlJsonMapper mapper = graphmlJsonMapperLoader.getMapper();

            assertAll("",//
                    () -> assertEquals("id", mapper.getId()),
                    () -> assertEquals("title", mapper.getLabel()),
                    () -> assertEquals(2, mapper.getLinks().size()),

                    () -> assertInstanceOf(Link.LinkCreateNode.class, mapper.getLinks().stream().toList().get(0)),
                    () -> assertEquals("id", ((Link.LinkCreateNode) mapper.getLinks().stream().toList().get(0)).id),
                    () -> assertEquals("designed by", ((Link.LinkCreateNode) mapper.getLinks().stream().toList().get(0)).linkLabel),
                    () -> assertEquals("name", ((Link.LinkCreateNode) mapper.getLinks().stream().toList().get(0)).label),
                    () -> assertEquals("credit.desiger", ((Link.LinkCreateNode) mapper.getLinks().stream().toList().get(0)).target),

                    () -> assertInstanceOf(Link.LinkToExistingNode.class, mapper.getLinks().stream().toList().get(1)),
                    () -> assertEquals("recommendations.fans_liked", ((Link.LinkToExistingNode) mapper.getLinks().stream().toList().get(1)).idTarget),
                    () -> assertEquals("recommended", ((Link.LinkToExistingNode) mapper.getLinks().stream().toList().get(1)).linkLabel)
            );


        }
    }

    /**
     * Return a Jackson ObjectNode from applying JsonPath to a stream -- we need it the other way around: Apply a
     * JsonPath to a Jackson ObjectNode
     */
    @Test
    void test() {
        String json = "{ \"a\": \"assume a stream here\"}";
        InputStream in = new ByteArrayInputStream(json.getBytes());

        Configuration conf = Configuration
                .builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .build();

        ObjectNode node = using(conf).parse(in).read("$");
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        List<Link> links = new ObjectMapper().readValue(json, new TypeReference<List<Link>>() {
        });
        assertNotNull(links);

        String result = new ObjectMapper().writeValueAsString(links);
        log.info("res={}", result);
    }

    @Test
    void testSerialize() throws JsonProcessingException {
        List<Link> links = new ArrayList<>();
        Link.LinkToExistingNode link1 = Link.LinkToExistingNode.of("recommended", "recommendations.fans_liked");
        links.add(link1);
        links.add(Link.LinkCreateNode.of("designed by", "credit.designer", "id", "name"));

        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Link.class);
        String result = mapper.writer().forType(listType).writeValueAsString(links);
        log.info("res={}", result);
        assertEquals(json, result);
    }
}
