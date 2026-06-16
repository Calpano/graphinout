package com.graphinout.base.cj;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CjAssertTest {

    @Test
    void test() {
        String actual = """
                {
                  "$schema": "https://j-s-o-n.org/schema/cj-8.0.0.json",
                  "graphs": [
                    { "nodes": [
                        { "id": "a" },
                        { "id": "c" },
                        { "id": "b" }
                      ]
                    }
                  ]
                }""";
        String expected = """
                {
                  "$schema": "https://j-s-o-n.org/schema/cj-8.0.0.json",
                  "graphs": [
                    { "nodes": [
                        { "id": "c" },
                        { "id": "a" },
                        { "id": "b" }
                      ]
                    }
                  ]
                }""";
        boolean b = CjAssert.xAssertThatIsSameCj(actual,expected,()->{});
        assertThat(b).isTrue();
    }


}
