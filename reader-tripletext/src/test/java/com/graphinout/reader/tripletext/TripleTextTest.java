package com.graphinout.reader.tripletext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripleTextTest {

    @Test
    void testMeta() {
        Triple<String, String, String> triple = TripleText.parseToTriple("A --is-- B .. meta");
        assertEquals("A", triple.s);
        assertEquals("is", triple.p);
        assertEquals("B", triple.o);
        assertEquals("meta", triple.meta);
    }

    @Test
    void testNoMeta() {
        Triple<String, String, String> triple = TripleText.parseToTriple("A --is-- B");
        assertEquals("A", triple.s);
        assertEquals("is", triple.p);
        assertEquals("B", triple.o);
    }

}
