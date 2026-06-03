package com.graphinout.reader.d2;

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

/** CJ → D2 → CJ → D2 roundtrip. */
public class D2RoundtripTest {

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToD2AndBackToCj(String displayPath, Resource textResource) throws IOException {
        String cjJsonIn = textResource.getContentAsString();
        ICjDocument cjDocument1 = CjDocuments.parseCjJsonString(displayPath, cjJsonIn);
        assertThat(cjDocument1).isNotNull();

        CjDocument2D2 out1 = new CjDocument2D2(cjDocument1);
        String d2_1 = out1.toD2();

        SingleInputSource src = SingleInputSource.of(displayPath + ".d2", d2_1);
        ICjDocument cjDocument2 = D2Reader.parseD2ToCjDocument(src);
        assertThat(cjDocument2).isNotNull();

        CjDocument2D2 out2 = new CjDocument2D2(cjDocument2);
        String d2_2 = out2.toD2();

        String n1 = normalize(d2_1);
        String n2 = normalize(d2_2);
        if (!n1.equals(n2)) {
            System.out.println("---- displayPath: " + displayPath);
            System.out.println("---- d2_1:\n" + d2_1);
            System.out.println("---- d2_2:\n" + d2_2);
            System.out.println("---- norm1:\n" + n1);
            System.out.println("---- norm2:\n" + n2);
        }
        assertThat(n2).isEqualTo(n1);
    }

    static String normalize(String d2) {
        if (d2 == null || d2.isBlank()) return "";
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String raw : d2.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;
            out.add(line);
        }
        java.util.Collections.sort(out);
        return String.join("\n", out);
    }
}
