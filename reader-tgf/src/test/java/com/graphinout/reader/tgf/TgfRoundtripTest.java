package com.graphinout.reader.tgf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSource;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Two tests: (1) find all /text/tgf resources, parse them into a CjDocument and then use the {@link TgfOutput} to
 * serialize back to TGF. Both strings should match. If required, build a TgfNormalizer to create a canonical TGF
 * string. Compare using the normalized versions. (2) use all CJ files, transform them to TGF and with theses TGF files,
 * run the test outline in (1).
 */
public class TgfRoundtripTest {

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    private static Stream<TestFileProvider.TestResource> tgfResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".tgf"));
    }

    /**
     * Test 2: Parse CJ files, transform to TGF, then parse back and compare.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToTgfAndBackToCj(String displayPath, Resource textResource) throws IOException {
        // Parse CJ JSON to CjDocument
        String content = textResource.getContentAsString();
        ICjDocument cjDocument1 = CjDocuments.parseCjJsonString(displayPath, content);
        assertThat(cjDocument1).isNotNull();

        // Convert CjDocument to TGF
        TgfOutput tgfOutput1 = new TgfOutput(cjDocument1);
        String tgfContent = tgfOutput1.toTgf();

        // Parse TGF back to CjDocument
        SingleInputSource tgfInputSource = SingleInputSource.of(displayPath + ".tgf", tgfContent);
        ICjDocument cjDocument2 = TgfReader.parseTgfToCjDocument(tgfInputSource);
        assertThat(cjDocument2).isNotNull();

        // Convert both back to TGF and compare normalized versions
        String tgf1 = tgfOutput1.toTgf();
        TgfOutput tgfOutput2 = new TgfOutput(cjDocument2);
        String tgf2 = tgfOutput2.toTgf();

        String normalizedTgf1 = normalizeTgf(tgf1);
        String normalizedTgf2 = normalizeTgf(tgf2);

        assertThat(normalizedTgf2).isEqualTo(normalizedTgf1);
    }

    /**
     * Test 1: Parse TGF files into CjDocument, serialize back to TGF, and compare with normalized versions.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("tgfResources")
    void shouldRoundtripTgfToCjAndBackToTgf(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        // Parse TGF to CjDocument
        ICjDocument cjDoc1 = TgfReader.parseTgfToCjDocument(singleInputSource);
        assertThat(cjDoc1).isNotNull();

        // Serialize CjDocument back to TGF
        TgfOutput tgfOutput = new TgfOutput(cjDoc1);
        String content2 = tgfOutput.toTgf();

        TestFileUtil.verifyOrRecord(textResource, "tgf_cj", content2, content, String::equals, this::normalizeTgf);
    }

    /**
     * Normalize TGF content to create a canonical representation for comparison. This handles differences in
     * whitespace, empty lines, and ordering where appropriate.
     */
    private String normalizeTgf(String tgf) {
        if (tgf == null || tgf.isEmpty()) {
            return "";
        }

        // Split by the section separator
        String[] parts = tgf.split("#", 2);
        String nodeSection = parts.length > 0 ? parts[0].trim() : "";
        String edgeSection = parts.length > 1 ? parts[1].trim() : "";

        // Normalize node section (sort lines and trim)
        String normalizedNodes = nodeSection.isEmpty() ? "" : String.join("\n", nodeSection.lines().map(String::trim).filter(line -> !line.isEmpty()).sorted().toList());

        // Normalize edge section (sort lines and trim)
        String normalizedEdges = edgeSection.isEmpty() ? "" : String.join("\n", edgeSection.lines().map(String::trim).filter(line -> !line.isEmpty()).sorted().toList());

        // Reconstruct normalized TGF
        StringBuilder result = new StringBuilder();
        if (!normalizedNodes.isEmpty()) {
            result.append(normalizedNodes).append("\n");
        }
        result.append("#\n");
        if (!normalizedEdges.isEmpty()) {
            result.append(normalizedEdges).append("\n");
        }

        return result.toString();
    }

}

