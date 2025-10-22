package com.graphinout.reader.tgf;

import com.graphinout.base.Gio2CjDocumentWriter;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Two tests:
 * (1) find all /text/tgf resources, parse them into a CjDocument and then use the {@link TgfOutput} to serialize back to TGF. Both strings should match. If required, build a TgfNormalizer to create a canonical TGF string. Compare using the normalized versions.
 * (2) use all CJ files, transform them to TGF and with theses TGF files, run the test outline in (1).
 */
public class TgfRoundtripTest {

    private static Stream<TestFileProvider.TestResource> tgfResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".tgf"));
    }

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    /**
     * Test 1: Parse TGF files into CjDocument, serialize back to TGF, and compare with normalized versions.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("tgfResources")
    void shouldRoundtripTgfToCjAndBackToTgf(String displayPath, Resource textResource) throws IOException {
        String originalContent = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, originalContent);

        // Parse TGF to CjDocument
        TgfReader tgfReader = new TgfReader();
        Gio2CjDocumentWriter cjDocWriter = new Gio2CjDocumentWriter();
        tgfReader.read(singleInputSource, cjDocWriter);
        ICjDocument cjDoc = cjDocWriter.resultDocument();

        assertThat(cjDoc).isNotNull();

        // Serialize CjDocument back to TGF
        TgfOutput tgfOutput = new TgfOutput(cjDoc);
        String roundtrippedContent = tgfOutput.toTgf();

        // Compare normalized versions
        String normalizedOriginal = normalizeTgf(originalContent);
        String normalizedRoundtripped = normalizeTgf(roundtrippedContent);

        assertThat(normalizedRoundtripped).isEqualTo(normalizedOriginal);
    }

    /**
     * Test 2: Parse CJ files, transform to TGF, then parse back and compare.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToTgfAndBackToCj(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        // Parse CJ JSON to CjDocument
        Cj2ElementsWriter cj2elements = new Cj2ElementsWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        Json2CjWriter json2Cj = new Json2CjWriter(cj2elements);
        jsonReader.read(singleInputSource, json2Cj);
        ICjDocument originalCjDoc = cj2elements.resultDoc();

        assertThat(originalCjDoc).isNotNull();

        // Convert CjDocument to TGF
        TgfOutput tgfOutput = new TgfOutput(originalCjDoc);
        String tgfContent = tgfOutput.toTgf();

        // Parse TGF back to CjDocument
        SingleInputSource tgfInputSource = SingleInputSource.of(displayPath + ".tgf", tgfContent);
        TgfReader tgfReader = new TgfReader();
        Gio2CjDocumentWriter cjDocWriter2 = new Gio2CjDocumentWriter();
        tgfReader.read(tgfInputSource, cjDocWriter2);
        ICjDocument roundtrippedCjDoc = cjDocWriter2.resultDocument();

        assertThat(roundtrippedCjDoc).isNotNull();

        // Convert both back to TGF and compare normalized versions
        String originalTgf = tgfOutput.toTgf();
        TgfOutput roundtrippedOutput = new TgfOutput(roundtrippedCjDoc);
        String roundtrippedTgf = roundtrippedOutput.toTgf();

        String normalizedOriginal = normalizeTgf(originalTgf);
        String normalizedRoundtripped = normalizeTgf(roundtrippedTgf);

        assertThat(normalizedRoundtripped).isEqualTo(normalizedOriginal);
    }

    /**
     * Normalize TGF content to create a canonical representation for comparison.
     * This handles differences in whitespace, empty lines, and ordering where appropriate.
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
        String normalizedNodes = nodeSection.isEmpty() ? "" :
            String.join("\n", nodeSection.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .sorted()
                .toList());

        // Normalize edge section (sort lines and trim)
        String normalizedEdges = edgeSection.isEmpty() ? "" :
            String.join("\n", edgeSection.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .sorted()
                .toList());

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

