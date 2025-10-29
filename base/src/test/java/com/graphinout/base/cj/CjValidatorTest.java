package com.graphinout.base.cj;

import com.graphinout.base.cj.util.CjValidator;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class CjValidatorTest {

    @Test
    void test() throws IOException {
//        "$schema": "https://calpano.github.io/connected-json/_attachments/cj-schema.json",
//                "$id": "https://j-s-o-n.org/schema/connected-json/5.0.0",

        SingleInputSourceOfString is = SingleInputSourceOfString.of("test", """
                {
                  "graphs": [
                    {
                      "id": "g1",
                      "label": [
                        { "value": "Simple Social Network" }
                      ],
                      "nodes": [
                        {
                          "id": "n1",
                          "label": [ { "value": "Alice" } ]
                        },
                        {
                          "id": "n2",
                          "label": [ { "value": "Bob" } ]
                        }
                      ],
                      "edges": [
                        {
                          "id": "e1",
                          "label": [ { "value": "knows" } ],
                          "endpoints": [
                            { "node": "n1", "direction": "out" },
                            { "node": "n2", "direction": "in" }
                          ]
                        }
                      ]
                    }
                  ]
                }""");

        boolean isValidCj = CjValidator.isValidCjCanonical(is);
        assertThat(isValidCj).isTrue();
    }

}
