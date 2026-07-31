package com.graphinout.reader.graphml;

import com.graphinout.testdata.TestFileUtil;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.stream.NoopCjStream;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import io.github.classgraph.Resource;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

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

    /**
     * Every GraphML fixture in the corpus is read, and BOTH outcomes are asserted.
     *
     * <p>A fixture tagged {@code --INVALIDgraphml} / {@code --INVALIDxml} must actually be rejected — it
     * either fails to parse outright (a file that is not even well-formed XML) or yields at least one
     * {@code Error}-level {@link ContentError}. Anything else is read strictly: recoverable warnings (e.g.
     * {@code <data>} referencing an undeclared {@code <key>}, which the reader auto-recovers as a string
     * key — issues.adoc I2) are tolerated, hard errors are not.
     *
     * <p>The invalid half used to be {@code if (isInvalid(...)) return;} — three stacked early returns that
     * made the test report green having asserted nothing, and not even as a visible skip. Two of those
     * three were provably unreachable: a {@code schema-1--INVALIDgraphml.graphml} special case "see #115"
     * and a hand-maintained {@code invalidFiles} denylist, both listing files the {@code isInvalid} tag
     * check above them had already excluded. They are gone; asserting the rejection is what the invalid
     * fixtures are FOR.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String displayName, Resource graphmlResource) throws Exception {
        boolean markedInvalid = TestFileUtil.isInvalid(graphmlResource, "graphml", "xml");
        log.info("Start to parse file [{}]", graphmlResource.getPath());

        List<ContentError> contentErrors = new ArrayList<>();
        boolean threw = false;
        try (SingleInputSource singleInputSource = inputSource(graphmlResource)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            graphmlReader.setContentErrorHandler(contentErrors::add);
            graphmlReader.read(singleInputSource, new NoopCjStream());
        } catch (Exception e) {
            if (!markedInvalid) throw e;
            threw = true;
        }
        List<ContentError> hardErrors = contentErrors.stream()
                .filter(e -> e.level == ContentError.ErrorLevel.Error).toList();

        if (markedInvalid) {
            assertTrue(threw || !hardErrors.isEmpty(),
                    "fixture is tagged --INVALID but the reader accepted it without a single Error: "
                            + graphmlResource.getPath() + " (errors seen: " + contentErrors + ")");
        } else {
            assertEquals(0, hardErrors.size(), "unexpected ERROR-level content errors: " + contentErrors);
        }
    }

}
