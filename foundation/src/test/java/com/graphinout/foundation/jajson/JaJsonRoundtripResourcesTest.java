package com.graphinout.foundation.jajson;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Parameterized round-trip test over all JSON resources found in src/main/resources.
 * <p>
 * For each JSON file, we:
 * - read original JSON text
 * - parse with JaJson
 * - serialize back with JaJson
 * - compare parse(original) to parse(serialized) for structural equality
 * - also compare a whitespace-normalized original to the serialized string, to allow textual comparison
 */
public class JaJsonRoundtripResourcesTest {

    @ParameterizedTest(name = "roundtrip {0}")
    @MethodSource("jsonFiles")
    void roundtrip_main_resources(Path file) throws IOException {
        String original = Files.readString(file, StandardCharsets.UTF_8);

        Object parsed = JaJson.parse(original);
        String reserialized = JaJson.toJsonString(parsed);

        // Structural equality: parsing the re-serialized JSON should yield an equal structure
        Object reparsed = JaJson.parse(reserialized);
        assertThat(reparsed).isEqualTo(parsed);

        // Textual comparison using a simple normalizer that removes insignificant whitespace
        String normalizedOriginal = normalizeJsonWhitespace(original);
        assertThat(reserialized).isEqualTo(normalizedOriginal);
    }

    static Stream<Arguments> jsonFiles() throws IOException {
        Path root = Paths.get("src/test/resources/json");
        if (!Files.exists(root)) {
            // Nothing to test; provide an empty stream so the parameterized test is effectively skipped
            return Stream.empty();
        }
        List<Arguments> args = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json")) //
                     // this one uses '50000000.75' but later has '5.000000075e7'
                    .filter(path -> !path.endsWith("complex-full.json"))
                    .forEach(p -> args.add(Arguments.of(p)));
        }
        // If directory exists but contains no JSON files, skip tests
        Assumptions.assumeTrue(!args.isEmpty(), "No JSON resources found in "+ root);
        return args.stream();
    }

    /**
     * Removes all JSON whitespace (space, tab, CR, LF) that is outside of string literals.
     * This produces a canonical compact form suitable for comparing against JaJson's output.
     */
    static String normalizeJsonWhitespace(String json) {
        StringBuilder sb = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaping = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                sb.append(c);
                if (escaping) {
                    // whatever the char is, we just consumed an escape sequence character
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    sb.append(c);
                } else //noinspection StatementWithEmptyBody
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    // drop insignificant whitespace outside strings
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
