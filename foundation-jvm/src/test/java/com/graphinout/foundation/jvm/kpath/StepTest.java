package com.graphinout.foundation.jvm.kpath;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepTest {

    @Test
    void validIndexes() {
        assertEquals(0, new Step("[0]").asIndex());
        assertEquals(7, new Step("[7]").asIndex());
        assertEquals(7, new Step("[007]").asIndex());
        assertEquals(Integer.MAX_VALUE, new Step("[" + Integer.MAX_VALUE + "]").asIndex());
    }

    @Test
    void invalidIndexesThrowIllegalArgument() {
        for (String step : List.of("[x]", "[]", "[-1]", "[+1]", "[ 1]", "[1.5]", "[99999999999]")) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Step(step), step);
            assertTrue(e.getMessage().contains(step), e.getMessage());
        }
    }

    @Test
    void wildcardsAndQuotedBracketsAreNotIndexes() {
        assertEquals(Step.Kind.ListAll, new Step("[*]").kind());
        assertEquals(Step.Kind.ListAll, new Step("[$items]").kind());
        assertEquals(Step.Kind.Map1, new Step("'[x]'").kind());
    }

    @Test
    void outOfRangeIndexReturnsNothing() {
        assertEquals(0, PathResolver.create().resolveAll(List.of("a", "b"), List.of("[5]")).size());
    }

    @Test
    void invalidStepFailsEvenIfNoDataReachesIt() {
        PathResolver resolver = PathResolver.create();
        Map<String, Object> root = Map.of("b", List.of("x"));
        // "a" matches nothing, so "[x]" is never applied to data; it must still fail
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveAll(root, List.of("a", "[x]")));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve1(List.of(), "[-1]"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve1(root, "[]"));
    }

}
