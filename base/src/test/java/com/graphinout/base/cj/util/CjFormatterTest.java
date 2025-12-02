package com.graphinout.base.cj.util;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.jajson.JaJson;
import com.graphinout.foundation.json.util.JsonCompactFormatter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class CjFormatterTest {

    static String jsonInput = """
            {"$schema":"https://calpano.github.io/connected-json/_attachments/cj-schema.json","$id":"https://j-s-o-n.org/schema/connected-json/5.0.0","graphs":[{"id":"world","nodes":[{"id":"canada"},{"id":"usa"}],"edges":[{"id":"trade_na","endpoints":[{"node":"canada"},{"node":"usa"}]}],"graphs":[{"id":"europe","label":[{"value":"European Partition"}],"nodes":[{"id":"france"},{"id":"germany"}],"edges":[{"id":"trade_eu","endpoints":[{"node":"france"},{"node":"germany"}]},{"id":"trade_transatlantic","endpoints":[{"node":"germany"},{"node":"usa"}]}]}]}]}
            """;
    static String formatted = """
            { "$schema": "https://calpano.github.io/connected-json/_attachments/cj-schema.json",
              "$id": "https://j-s-o-n.org/schema/connected-json/5.0.0",
              "graphs":
                [ { "id": "world",
                    "nodes":
                      [ { "id": "canada" },
                        { "id": "usa" }
                      ],
                    "edges":
                      [ { id": "trade_na", "endpoints": [ { "node": "canada" }, { "node": "usa" } ] } ],
                    "graphs":
                      [ { "id": "europe",
                          "label": "European Partition",
                          "nodes":
                            [ { "id": "france" },
                              { "id": "germany" }
                            ],
                          "edges":
                            [ { "id": "trade_eu", "endpoints": [ { "node": "france" }, { "node": "germany" } ] },
                              { "id": "trade_transatlantic", "endpoints": [ { "node": "germany" }, { "node": "usa" } ] }
                            ]
                        }
                      ]
                  }
                ]
            }
            """;

    /**
     * TODO fine-tune force multi-lines.
     * If child lines are only 1, keep even container in 1 line
     */
    @Test
    @Disabled
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
