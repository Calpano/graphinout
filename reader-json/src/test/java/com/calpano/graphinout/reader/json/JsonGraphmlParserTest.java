package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;

import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMapping;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMappingLoader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

class JsonGraphmlParserTest {

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void read() throws Exception {
        GraphmlJsonMapping graphmlPathBuilder = pathBuilder();
        Path inputSource = Paths.get("src", "test", "resources", "sample", "boardgames_40.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            JsonGraphmlParser graphmlReader = new JsonGraphmlParser(singleInputSource, gioWriter, graphmlPathBuilder);
            graphmlReader.read();
            System.out.println(outputSink.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GraphmlJsonMapping pathBuilder() throws Exception {
        GraphmlJsonMapping pathBuilder;
        Path inputSource = Paths.get("src", "test", "resources", "json-mapper", "json-mapper-1.json");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
             pathBuilder = new GraphmlJsonMappingLoader(singleInputSource).getMapper();
        }
        return pathBuilder;
    }
}
