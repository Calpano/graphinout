package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Roundtrip tests for the DDot reader/writer.
 * (1) DDot file -> CjDocument -> DDot string : normalized strings should match.
 * (2) CJ file -> DDot string -> CjDocument -> DDot string : the two DDot strings should match (normalized).
 */
public class DDotRoundtripTest {

    private static Stream<TestFileProvider.TestResource> ddotResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".ddot"));
    }

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToDDotAndBackToCj(String displayPath, Resource textResource) throws IOException {
        String cjJsonIn = textResource.getContentAsString();
        ICjDocument cjDocument1 = CjDocuments.parseCjJsonString(displayPath, cjJsonIn);
        assertThat(cjDocument1).isNotNull();

        DDotOutput out1 = new DDotOutput(cjDocument1);
        String ddot1 = out1.toDDot();

        SingleInputSource ddotInput = SingleInputSource.of(displayPath + ".ddot", ddot1);
        ICjDocument cjDocument2 = DDotReader.parseDDotToCjDocument(ddotInput);
        assertThat(cjDocument2).isNotNull();

        DDotOutput out2 = new DDotOutput(cjDocument2);
        String ddot2 = out2.toDDot();

        String n1 = normalizeDDot(ddot1);
        String n2 = normalizeDDot(ddot2);
        if (!n1.equals(n2)) {
            System.out.println("---- CJ Input:\n" + cjJsonIn);
            System.out.println("---- DDot 1:\n" + ddot1);
            System.out.println("---- DDot 2:\n" + ddot2);
        }
        assertThat(n2).isEqualTo(n1);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ddotResources")
    void shouldRoundtripDDotToCjAndBackToDDot(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        ICjDocument cjDoc1 = DDotReader.parseDDotToCjDocument(singleInputSource);
        assertThat(cjDoc1).isNotNull();

        DDotOutput out = new DDotOutput(cjDoc1);
        String content2 = out.toDDot();

        TestFileUtil.verifyOrRecord(textResource, "ddot_cj", content2, content, String::equals, this::normalizeDDot);
    }

    /** Normalize: drop comments, honor on/off switches, expand continuation lines, trim, sort. */
    String normalizeDDot(String ddot) {
        if (ddot == null || ddot.isBlank()) return "";
        java.util.List<String> out = new java.util.ArrayList<>();
        String subject = null;
        boolean enabled = true;
        for (String raw : ddot.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;
            if (line.equals("ddot.it/off")) { enabled = false; continue; }
            if (line.equals("ddot.it/on")) { enabled = true; continue; }
            if (!enabled) continue;

            String body = line;
            boolean isContinuation = body.startsWith("..");
            if (isContinuation) body = body.substring(2).trim();
            String[] parts = body.split("\\s*\\.\\.\\s*", -1);
            String s, p, o;
            if (isContinuation) {
                if (subject == null || parts.length != 2) continue;
                s = subject;
                p = parts[0].trim();
                o = parts[1].trim();
            } else {
                if (parts.length != 3) continue;
                s = parts[0].trim();
                p = parts[1].trim();
                o = parts[2].trim();
                subject = s;
            }
            out.add(s + " .. " + p + " .. " + o);
        }
        java.util.Collections.sort(out);
        return String.join("\n", out);
    }
}
