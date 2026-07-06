package com.graphinout.reader.pajek;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Two roundtrip dimensions:
 * <ol>
 *   <li>Pajek (.net) → CjDocument → Pajek text → CjDocument → Pajek text; compare both Pajek texts.</li>
 *   <li>Canonical CJ → Pajek text → CjDocument → Pajek text; compare both Pajek texts.</li>
 * </ol>
 * Comparison is on normalized Pajek (sections split, lines sorted) so ordering differences don't cause false failures.
 *
 * <p><b>Known losses (partial support per format registry):</b> edge weights/labels, node coordinates,
 * typed-edge relation names. These are tested (and documented as gaps) in {@code FeatureRoundtripTest}.
 */
public class PajekRoundtripTest {

    private static Stream<TestFileProvider.TestResource> pajekResources() {
        return TestFileProvider.getAllTestResources()
                .filter(res -> res.resource().getPath().endsWith(".net"));
    }

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    /** Test 1: .net file → CJ → Pajek text1 → CJ → Pajek text2; text1 and text2 must match. */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("pajekResources")
    void shouldRoundtripPajekToCjAndBackToPajek(String displayPath, Resource resource) throws IOException {
        String content = resource.getContentAsString();
        SingleInputSource src = SingleInputSource.of(displayPath, content);

        ICjDocument cjDoc1 = PajekReader.parsePajekToCjDocument(src);
        assertThat(cjDoc1).isNotNull();

        String pajek1 = new PajekOutput(cjDoc1).toPajek();

        ICjDocument cjDoc2 = PajekReader.parsePajekToCjDocument(
                SingleInputSource.of(displayPath + ".roundtrip", pajek1));
        assertThat(cjDoc2).isNotNull();

        String pajek2 = new PajekOutput(cjDoc2).toPajek();

        if (!normalizePajek(pajek1).equals(normalizePajek(pajek2))) {
            System.out.println("--- Original .net ---\n" + content);
            System.out.println("--- Pajek pass 1 ---\n" + pajek1);
            System.out.println("--- CJ after pass 1 ---\n" + CjDocuments.toJsonString(cjDoc2));
            System.out.println("--- Pajek pass 2 ---\n" + pajek2);
        }
        assertThat(normalizePajek(pajek2)).isEqualTo(normalizePajek(pajek1));
    }

    /** Test 2: canonical CJ → Pajek text1 → CJ → Pajek text2; text1 and text2 must match. */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToPajekAndBackToCj(String displayPath, Resource resource) throws IOException {
        String cjJson = resource.getContentAsString();
        ICjDocument cjDoc1 = CjDocuments.parseCjJsonString(displayPath, cjJson);
        assertThat(cjDoc1).isNotNull();

        String pajek1 = new PajekOutput(cjDoc1).toPajek();

        ICjDocument cjDoc2 = PajekReader.parsePajekToCjDocument(
                SingleInputSource.of(displayPath + ".net", pajek1));
        assertThat(cjDoc2).isNotNull();

        String pajek2 = new PajekOutput(cjDoc2).toPajek();

        if (!normalizePajek(pajek1).equals(normalizePajek(pajek2))) {
            System.out.println("--- CJ input ---\n" + cjJson);
            System.out.println("--- Pajek pass 1 ---\n" + pajek1);
            System.out.println("--- CJ after pass 1 ---\n" + CjDocuments.toJsonString(cjDoc2));
            System.out.println("--- Pajek pass 2 ---\n" + pajek2);
        }
        assertThat(normalizePajek(pajek2)).isEqualTo(normalizePajek(pajek1));
    }

    private enum Sec { NONE, VERTICES, ARCS, EDGES }

    private String normalizePajek(String pajek) {
        if (pajek == null || pajek.isBlank()) return "";

        List<String> vertexLines = new ArrayList<>();
        List<String> arcLines = new ArrayList<>();
        List<String> edgeLines = new ArrayList<>();

        Sec sec = Sec.NONE;
        for (String raw : pajek.lines().toList()) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("%")) continue;
            String up = line.toUpperCase();
            if (up.startsWith("*VERTICES")) { sec = Sec.VERTICES; continue; }
            if (up.startsWith("*ARCS"))     { sec = Sec.ARCS;     continue; }
            if (up.startsWith("*EDGES"))    { sec = Sec.EDGES;    continue; }
            if (line.startsWith("*"))       { sec = Sec.NONE;     continue; }
            switch (sec) {
                case VERTICES -> vertexLines.add(line);
                case ARCS     -> arcLines.add(line);
                case EDGES    -> edgeLines.add(line);
                default       -> {}
            }
        }

        Collections.sort(vertexLines);
        Collections.sort(arcLines);
        Collections.sort(edgeLines);

        StringBuilder b = new StringBuilder();
        b.append("*Vertices ").append(vertexLines.size()).append("\n");
        vertexLines.forEach(l -> b.append(l).append("\n"));
        if (!arcLines.isEmpty()) {
            b.append("*Arcs\n");
            arcLines.forEach(l -> b.append(l).append("\n"));
        }
        if (!edgeLines.isEmpty()) {
            b.append("*Edges\n");
            edgeLines.forEach(l -> b.append(l).append("\n"));
        }
        return b.toString();
    }
}
