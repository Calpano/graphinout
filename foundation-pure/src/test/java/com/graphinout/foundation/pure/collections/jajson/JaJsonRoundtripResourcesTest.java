package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.testdata.TestFileProvider;
import com.graphinout.foundation.pure.text.JsonFormatting;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Parameterized round-trip test over all JSON resources found in src/main/resources.
 * <p>
 * For each JSON file, we: - read original JSON text - parse with JaJson - serialize back with JaJson - compare
 * parse(original) to parse(serialized) for structural equality - also compare a whitespace-normalized original to the
 * serialized string, to allow textual comparison
 */
public class JaJsonRoundtripResourcesTest {

    static Stream<TestFileProvider.TestResource> jsonFiles() {
        return TestFileProvider.jsonResources().filter(res ->
                // this one uses '50000000.75' but later has '5.000000075e7'
                !res.asPath().endsWith("complex-full.json")
        );
    }

    @ParameterizedTest(name = "roundtrip {0}")
    @MethodSource("jsonFiles")
    void roundtrip_main_resources(String displayPath, Resource res) throws IOException {
        String original = res.getContentAsString();

        Object parsed = JaJson.parse(original);
        String reserialized = JaJson.toJsonString(parsed);

        // Structural equality: parsing the re-serialized JSON should yield an equal structure
        Object reparsed = JaJson.parse(reserialized);
        assertThat(reparsed).isEqualTo(parsed);

        // Textual comparison using a simple normalizer that removes insignificant whitespace
        String normalizedOriginal = JsonFormatting.normalizeJsonWhitespace(original);
        assertThat(reserialized).isEqualTo(normalizedOriginal);
    }

}
