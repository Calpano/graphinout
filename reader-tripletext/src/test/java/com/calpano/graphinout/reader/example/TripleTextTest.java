package com.calpano.graphinout.reader.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripleTextTest {

    @Test
    void testNoMeta() {
        Triple triple = TripleText.parseToTriple("A --is-- B");
        assertEquals("A", triple.s);
        assertEquals("is", triple.p);
        assertEquals("B", triple.o);
    }

    @Test
    void testMeta() {
        Triple triple = TripleText.parseToTriple("A --is-- B .. meta");
        assertEquals("A", triple.s);
        assertEquals("is", triple.p);
        assertEquals("B", triple.o);
        assertEquals("meta", triple.meta);
    }

}
