package com.graphinout.base.cj.util;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class CjFormatterTest {

    static String jsonInput = """
            {"$schema":"https://calpano.github.io/connected-json/_attachments/cj-schema.json","$id":"https://j-s-o-n.org/schema/connected-json/8.0.0","graphs":[{"id":"world","nodes":[{"id":"canada"},{"id":"usa"}],"edges":[{"id":"trade_na","endpoints":[{"node":"canada"},{"node":"usa"}]}],"graphs":[{"id":"europe","label":{"entries":[{"value":"European Partition"}]},"nodes":[{"id":"france"},{"id":"germany"}],"edges":[{"id":"trade_eu","endpoints":[{"node":"france"},{"node":"germany"}]},{"id":"trade_transatlantic","endpoints":[{"node":"germany"},{"node":"usa"}]}]}]}]}
            """;
    static String formatted = """
            { "$schema": "https://j-s-o-n.org/schema/cj-8.0.0.json",
              "graphs":
                [ { "id": "world",
                    "nodes":
                      [ { "id": "canada", "data": null },
                        { "id": "usa", "data": null }
                      ],
                    "edges":
                      [ { "id": "trade_na", "endpoints": [ { "node": "canada" }, { "node": "usa" } ] }
                      ],
                    "graphs":
                      [ { "id": "europe",
                          "label": "European Partition",
                          "nodes":
                            [ { "id": "france", "data": null },
                              { "id": "germany", "data": null }
                            ],
                          "edges":
                            [ { "id": "trade_eu", "endpoints": [ { "node": "france" }, { "node": "germany" } ] },
                              { "id": "trade_transatlantic", "endpoints": [ { "node": "germany" }, { "node": "usa" } ] }
                            ]
                        }
                      ]
                  }
                ]
            }""";

    /**
     * Pins the CJ compact-formatter's CURRENT output.
     *
     * <p>This was {@code @Disabled} with no reason at all. Two separate things were wrong. First, the
     * input used {@code "label":[...]}, which is not CJ — the canonical form is
     * {@code "label":{"entries":[...]}} — so the test died in {@code JsonReaderImpl} before the formatter
     * ever ran ({@code Expected exactly one element matching 'Array', but found none in [Label]}). Second,
     * the expected string was aspirational rather than actual: it encoded the TODO below, and carried a
     * typo ({@code &#123; id":} — missing its opening quote) that could never have matched.
     *
     * <p>STILL WANTED (the original TODO): force-multi-line is too eager. A container whose children fit
     * on one line should stay on one line, so {@code "edges": [ &#123;one edge&#125; ]} should not be
     * broken across three lines as it is here. Implementing that will fail this test — update the
     * expectation then, deliberately.
     *
     * <p>Also visible and probably unwanted: nodes serialize an explicit {@code "data": null}.
     */
    @Test
    void test() throws IOException {
        // parse JSON to CJ Doc
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("test", jsonInput);

        Map<String, Object> jaDoc = cjDoc.toJaJsonMap();
        String actual = JsonCompactFormatter.formatCompact(jaDoc, 120, Set.of("nodes", "edges", "graphs"));
        assertThat(actual).isEqualTo(formatted);
    }

    @Test
    void testArray() throws IOException {
        List<Object> jaJsonInput = List.of(2, 3, 4);
        String formatted = """
                [ 2, 3, 4 ]""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 120, Set.of("nodes", "edges", "graphs"));
        assertThat(actual).isEqualTo(formatted);
    }

    @Test
    void testArrayOfObjects() throws IOException {
        List<Object> jaJsonInput = List.of(Map.of("a", 5),
                JaJson.createMap().put("b", 6).put("c", 7).build(), 4);
        String formatted = """
                [ { "a": 5 }, { "b": 6, "c": 7 }, 4 ]""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 120, Set.of("nodes", "edges", "graphs"));
        assertThat(actual).isEqualTo(formatted);
    }


}
