package com.graphinout.base.cj.anonymize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The character rule used by the anonymizers: letters -> X/x, digits -> 0, everything else kept. */
class AnonymizerTest {

    @Test
    void charRulePreservesShape() {
        assertEquals("Xxxxx Xxxxx 00!", Anonymizer.text("Hello World 42!"));
        assertEquals("Xxx-xxx_X.x/0", Anonymizer.text("Abc-def_G.h/9"));
        // non-ASCII: cased letters mapped (Ü->X, é->x), case-less kept as letters too
        assertEquals("Xxxx xxxX", Anonymizer.text("Über cafÉ"));
    }
}
