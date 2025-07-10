package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMapping;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMappingLoader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.slf4j.LoggerFactory.getLogger;

class JsonGraphmlParserTest {

    private static final Logger log = getLogger(JsonGraphmlParserTest.class);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void read() throws Exception {
        GraphmlJsonMapping graphmlPathBuilder = pathBuilder();
        Path inputSource = Paths.get("src", "test", "resources", "sample", "boardgames_40.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content); //
             OutputSink outputSink = new InMemoryOutputSink()) {
            GioWriter gioWriter = new Gio2GraphmlWriter(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            JsonGraphmlParser graphmlReader = new JsonGraphmlParser(singleInputSource, gioWriter, graphmlPathBuilder, error -> log.warn("Error: " + error));
            graphmlReader.read();
            System.out.println(outputSink.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
    }

    private GraphmlJsonMapping pathBuilder() throws Exception {
        GraphmlJsonMapping pathBuilder;
        Path inputSource = Paths.get("src", "test", "resources", "json-mapper", "json-mapper-1.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            pathBuilder = GraphmlJsonMappingLoader.loadMapping(singleInputSource);
        }
        return pathBuilder;
    }

}
