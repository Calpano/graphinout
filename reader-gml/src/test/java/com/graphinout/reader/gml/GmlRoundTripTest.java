package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.text.StringFormatter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

class GmlRoundTripTest {

    private static final Logger log = getLogger(GmlRoundTripTest.class);
    @ParameterizedTest
    @MethodSource("com.graphinout.reader.gml.GmlReaderTest#gmlResources")
    void testRoundTrip(String displayPath, Resource resource) throws IOException {
        // GML to CJ
        String gmlContent = resource.getContentAsString();
        List<Object> expectedList = GmlTokenizer.tokenizeToList(gmlContent);

        SingleInputSource inputSource = SingleInputSource.of("gml-test", gmlContent);
        ICjDocument cjDocument = GmlReader.parseGmlToCjDocument(inputSource);
        log.info("CJ JSON: "+ CjDocuments.toJsonString(cjDocument));

        // CJ to GML
        GmlOutput  gmlOutput = new GmlOutput(cjDocument);
        List<Object> actualList = gmlOutput.toGmlList();

        String newGmlContent = gmlOutput.toGml();
        log.info("Result GML: \n"+newGmlContent);

        System.out.println("[DEBUG_LOG] Expected (normalized):\n" + expectedList);
        System.out.println("[DEBUG_LOG] Actual   (normalized):\n" + actualList);

        GmlAssert.assertEquals(expectedList, actualList);
    }

    @Test
    void testRoundTrip_1() throws IOException {
        Resource res = TestFileUtil.resource("text/gml/lesmiserables-small.gml");
        testRoundTrip(res.getPath(), res);
    }

    private String normalizeGml(String gml) {
        String normSpace = StringFormatter.normalizeLineBreaks(gml);
        return Arrays.stream(normSpace.trim().split("\\s*\\n+"))
                .flatMap(
                        // split via regex '\w+[ ].*' at first space (but not other spaces)
                        s->  Arrays.stream(s.trim().split(" ", 2))
                        )
                .filter(s->!s.isBlank())
                .sorted().collect(Collectors.joining("\n"));
    }

}
