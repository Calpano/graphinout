package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GmlRoundTripTest {

    @ParameterizedTest
    @MethodSource("com.graphinout.reader.gml.GmlReaderTest#gmlResources")
    void testRoundTrip(String displayPath, Resource resource) throws IOException {
        // GML to CJ
        String gmlContent = resource.getContentAsString();
        SingleInputSource inputSource = SingleInputSource.of("gml-test", gmlContent);
        ICjDocument cjDocument = GmlReader.parseGmlToCjDocument(inputSource);

        // CJ to GML
        GmlOutput gmlOutput = new GmlOutput(cjDocument);
        String newGmlContent = gmlOutput.toGml();

        assertEquals(normalizeGml(gmlContent), normalizeGml(newGmlContent));
    }

    private String normalizeGml(String gml) {
        return Arrays.stream(gml.trim().split("\\s*\\r?\\n+"))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

}
