package com.graphinout.reader.tripletext;

import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for TripleText following the plan in test-patterns.adoc Phase 1 handled by TripleTextReaderTest (parser
 * stability over resources). This class focuses on Phase 2 round-trip conversion with a normalizer.
 */
public class TripleTextRoundtripTest {

    private static Stream<TestFileProvider.TestResource> cjResources() {
        java.util.Set<String> supported = java.util.Set.of(
                "minimal.cj.json",
                "nested-graphs.cj.json",
                "custom-data-simple.cj.json",
                "custom-data-simple2.cj.json",
                "custom-data-simple3.cj.json",
                "ports-simple.cj.json"
        );
        return TestFileProvider.cjResourcesCanonical()
                .filter(res -> supported.contains(java.nio.file.Paths.get(res.resource().getPath()).getFileName().toString()));
    }

    private static Stream<TestFileProvider.TestResource> tripleTextResources() {
        return TestFileProvider.getAllTestResources().filter(res -> {
            String p = res.resource().getPath();
            // Only use TripleText samples from our dedicated folder
            return p.contains("text/tripletext/") && (p.endsWith(".tt") || p.endsWith(".tripletext") || p.endsWith("triple.txt"));
        });
    }

    // Test 2: CJ -> TripleText -> CJ (compare normalized TripleText)
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjThroughTripleText(String displayPath, Resource cjResource) throws IOException {
        String cjJson = cjResource.getContentAsString();
        SingleInputSource cjInput = SingleInputSource.of(displayPath, cjJson);

        // Parse CJ JSON into CjDocument
        CjWriter2CjDocumentWriter cj2elements = new CjWriter2CjDocumentWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        Json2CjWriter json2Cj = new Json2CjWriter(cj2elements);
        jsonReader.read(cjInput, json2Cj);
        ICjDocument originalCj = cj2elements.resultDoc();
        Assertions.assertNotNull(originalCj);

        // CJ -> TripleText
        TripleTextOutput out1 = new TripleTextOutput(originalCj);
        String tripleText = out1.toTripleText();

        // TripleText -> CJ
        SingleInputSource ttInput = SingleInputSource.of(displayPath + ".tt", tripleText);
        TripleTextReader reader = new TripleTextReader();
        CjWriter2CjDocumentWriter elementsWriter2 = new CjWriter2CjDocumentWriter();
        CjStream2CjWriter streamToWriter2 = new CjStream2CjWriter(elementsWriter2);
        reader.read(ttInput, streamToWriter2);
        ICjDocument roundtrippedCj = elementsWriter2.resultDoc();
        Assertions.assertNotNull(roundtrippedCj);

        // Back to TripleText on both and compare normalized
        String tripleText1 = new TripleTextOutput(originalCj).toTripleText();
        String tripleText2 = new TripleTextOutput(roundtrippedCj).toTripleText();

        Assertions.assertEquals(normalizeTripleText(tripleText1), normalizeTripleText(tripleText2));
    }

    // Test 1: TripleText -> CJ -> TripleText (compare normalized)
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("tripleTextResources")
    void shouldRoundtripTripleText(String displayPath, Resource textResource) throws IOException {
        String content1 = textResource.getContentAsString();
        SingleInputSource input = SingleInputSource.of(displayPath, content1);

        ICjDocument cjDoc = TripleTextReader.parseToCjDocument(input);
        Assertions.assertNotNull(cjDoc);

        // Serialize back to TripleText
        TripleTextOutput out = new TripleTextOutput(cjDoc);
        String content2 = out.toTripleText();

        String nOrig = normalizeTripleText(content1);
        String nRound = normalizeTripleText(content2);
        Assertions.assertEquals(nOrig, nRound);
    }

    private String normalizeTripleText(String text) {
        if (text == null || text.isBlank()) return "";
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            Triple<String, String, String> t = TripleText.parseToTriple(line);
            if (t == null) continue; // skip non-triple or empty lines
            String s = t.s == null ? "" : t.s.trim();
            String p = t.p == null ? "" : t.p.trim();
            String o = t.o == null ? "" : t.o.trim();
            if (s.isEmpty() || p.isEmpty() || o.isEmpty()) continue;
            lines.add(s + " -- " + p + " -- " + o);
        }
        return lines.stream().sorted(Comparator.naturalOrder()).collect(Collectors.joining("\n", "", lines.isEmpty() ? "" : "\n"));
    }

}
