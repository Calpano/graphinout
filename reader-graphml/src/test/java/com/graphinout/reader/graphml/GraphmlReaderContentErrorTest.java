package com.graphinout.reader.graphml;

import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.api.NoopCjStream;
import com.graphinout.base.reader.Location;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

    private final List<String> invalidFiles = new ArrayList<>();


    @Test
    void elementsGraphmlDoesNotAllowCharacter_invalid_root() throws Exception {
        Path inputSource = Paths.get("../base/src/test/resources/xml/graphml/synthetic/invalidgraphml-root.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.setContentErrorHandler(contentErrors::add);

            ICjStream cjStream = new NoopCjStream();
            graphmlReader.read(singleInputSource, cjStream);
            List<ContentError> contentErrorsResult = contentErrors.stream().toList();
            assertEquals(2, contentErrorsResult.size());
            ContentError first = contentErrorsResult.getFirst();
            {
                assertEquals(ContentError.ErrorLevel.Error, first.getLevel());
                assertEquals(Location.of(2, 9), first.getLocation());
                assertEquals("While parsing 2:9\n" +
                        "Message: XML Element <myroot> is not a Graphml tag and not allowing XML here. XmlParseContext{elementStack=[], mode=Graphml}", first.getMessage());
            }
            ContentError second = contentErrorsResult.get(1);
            {
                assertEquals(ContentError.ErrorLevel.Error, second.getLevel());
                assertEquals("""
                        While parsing 4:10
                        Message: Unexpected content ('
                            Hello
                        ') outside Graphml content tags.""", second.getMessage());
                assertEquals(Location.of( 4,10), second.getLocation());

            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String displayName, Resource graphmlResource) throws Exception {
        if (TestFileUtil.isInvalid(graphmlResource, "graphml", "xml")) {
            return;
        }

        // see #115
        if (graphmlResource.getPath().endsWith("schema-1--INVALIDgraphml.graphml"))
            return;

        log.info("Start to parse file [{}]", graphmlResource.getPath());

        if (invalidFiles.stream().anyMatch(s -> graphmlResource.getPath().endsWith(s))) {
            log.info("This file is known as invalid.");
            return;
        }
        try (SingleInputSource singleInputSource = inputSource(graphmlResource)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.setContentErrorHandler(contentErrors::add);
            ICjStream cjStream = new NoopCjStream();
            graphmlReader.read(singleInputSource, cjStream);
            assertEquals(0, contentErrors.stream().count());
        }
    }

    @BeforeEach
    void setUp() {
        invalidFiles.add("xml/graphml/synthetic/root--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/haitimap2--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/greek2--INVALIDgraphml.graphml");
    }


}
