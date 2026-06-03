package com.graphinout.reader.mermaid;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Roundtrip tests. The writer emits flowchart-only Mermaid, so this only covers a subset of the reader's
 * capabilities. We compare normalized edge-lists rather than literal Mermaid strings.
 */
public class MermaidRoundtripTest {

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToMermaidAndBackToCj(String displayPath, Resource textResource) throws IOException {
        String cjJsonIn = textResource.getContentAsString();
        ICjDocument cjDocument1 = CjDocuments.parseCjJsonString(displayPath, cjJsonIn);
        assertThat(cjDocument1).isNotNull();

        MermaidOutput out1 = new MermaidOutput(cjDocument1);
        String mermaid1 = out1.toMermaid();

        SingleInputSource inputSrc = SingleInputSource.of(displayPath + ".mmd", mermaid1);
        ICjDocument cjDocument2 = MermaidReader.parseMermaidToCjDocument(inputSrc);
        assertThat(cjDocument2).isNotNull();

        MermaidOutput out2 = new MermaidOutput(cjDocument2);
        String mermaid2 = out2.toMermaid();

        String n1 = normalize(mermaid1);
        String n2 = normalize(mermaid2);
        if (!n1.equals(n2)) {
            System.out.println("---- displayPath: " + displayPath);
            System.out.println("---- mermaid1:\n" + mermaid1);
            System.out.println("---- mermaid2:\n" + mermaid2);
            System.out.println("---- norm1:\n" + n1);
            System.out.println("---- norm2:\n" + n2);
        }
        assertThat(n2).isEqualTo(n1);
    }

    /** Sort & trim node and edge lines (ignoring the header). */
    static String normalize(String mermaid) {
        if (mermaid == null || mermaid.isBlank()) return "";
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String raw : mermaid.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("flowchart") || line.startsWith("graph")) continue;
            out.add(line);
        }
        java.util.Collections.sort(out);
        return String.join("\n", out);
    }
}
