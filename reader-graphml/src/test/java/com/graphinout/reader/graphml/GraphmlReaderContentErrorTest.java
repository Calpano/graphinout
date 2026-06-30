package com.graphinout.reader.graphml;

import com.graphinout.testdata.TestFileUtil;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.stream.NoopCjStream;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.base.TestFileUtil2.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

    private final List<String> invalidFiles = new ArrayList<>();


    @Test
    void elementsGraphmlDoesNotAllowCharacter_invalid_root() throws Exception {
        String resourcePath = "xml/graphml/synthetic/root--INVALIDgraphml.graphml";
        Resource resource = TestFileUtil.resource(resourcePath);
        assertThat(resource).isNotNull();
        String content = resource.getContentAsString();
        try (SingleInputSource singleInputSource = SingleInputSource.of(resourcePath, content)) {
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
                assertEquals(
                        "XML Element <myroot> is not a Graphml tag and not allowing XML here. XmlParseContext{elementStack=[], mode=Graphml}", first.getMessage());
            }
            ContentError second = contentErrorsResult.get(1);
            {
                assertEquals(ContentError.ErrorLevel.Error, second.getLevel());
                assertEquals("""
                        Unexpected content ('
                            Hello
                        ') outside Graphml content tags.""", second.getMessage());
                assertEquals(Location.of(4, 10), second.getLocation());

            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#graphmlResources")
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
            // Recoverable warnings (e.g. <data> referencing an undeclared <key>, which the reader auto-recovers as a
            // string key — see issues.adoc I2) are tolerated; assert only that no hard Error-level content errors occur.
            List<ContentError> hardErrors = contentErrors.stream()
                    .filter(e -> e.level == ContentError.ErrorLevel.Error).toList();
            assertEquals(0, hardErrors.size(), "unexpected ERROR-level content errors: " + contentErrors);
        }
    }

    @BeforeEach
    void setUp() {
        invalidFiles.add("xml/graphml/synthetic/root--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/haitimap2--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/greek2--INVALIDgraphml.graphml");
    }


}
