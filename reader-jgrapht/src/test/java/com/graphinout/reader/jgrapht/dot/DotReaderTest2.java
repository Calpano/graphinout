package com.graphinout.reader.jgrapht.dot;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.GioReader;
import com.graphinout.base.ReaderTests;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class DotReaderTest extends AbstractReaderTest {

    private static final Logger log = getLogger(DotReaderTest.class);

    protected List<ContentError> expectedErrors(String resourceName) {
        if (resourceName.endsWith("Call_Graph.gv")) {
            return List.of(new ContentError(ContentError.ErrorLevel.Error, "at line 35:22 extraneous input ',' expecting {'{', '}', GRAPH, NODE, EDGE, SUBGRAPH, NUMBER, STRING, ID, HTML_STRING}", null));
        }
        if (resourceName.endsWith("libinput-stack-xorg.gv")) {
            return List.of(new ContentError(ContentError.ErrorLevel.Error, "at line 6:15 extraneous input ';' expecting {']', NUMBER, STRING, ID, HTML_STRING}", null));
        }
        if (resourceName.endsWith("oberon.gv")) {
            return List.of(new ContentError(ContentError.ErrorLevel.Error, "at line 32:6 extraneous input ',' expecting {'{', '}', GRAPH, NODE, EDGE, SUBGRAPH, NUMBER, STRING, ID, HTML_STRING}", null));
        }
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new DotReader());
    }


    @Test
    void test() {
        GioReader gioReader = new DotReader();
        ReaderTests.forEachReadableResource(gioReader, resource -> {
            try {
                String content = resource.getContentAsString();
                InputSource inputSource = SingleInputSourceOfString.of(resource.getPath(), content);
                DotReader.readToCjJson(inputSource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testOneResource() throws IOException {
        Resource resource = TestFileUtil.resource("text/dot/synthetics/synthetic2.dot");
        assert resource != null;
        String content = resource.getContentAsString();
        InputSource inputSource = SingleInputSourceOfString.of(resource.getPath(), content);
        DotReader.readToCjJson(inputSource);
        log.info("Read:\n" + content);
    }

}
