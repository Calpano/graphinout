package com.graphinout.foundation.jvm.kpath;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KPathsTest {

    @Test
    void splitsUnquotedSteps() {
        assertEquals(List.of("a", "b", "c"), KPaths.of("a/b/c"));
        assertEquals(List.of("..", "graphs", "[$graph]", "nodes"), KPaths.of("../graphs/[$graph]/nodes"));
    }

    @Test
    void ignoresOneLeadingSlash() {
        assertEquals(List.of("a", "b"), KPaths.of("/a/b"));
        assertEquals(List.of("", "a"), KPaths.of("//a"));
    }

    @Test
    void keepsEmptySteps() {
        assertEquals(List.of("a", "", "b"), KPaths.of("a//b"));
        assertEquals(List.of(""), KPaths.of(""));
    }

    @Test
    void quotedStepMayContainSlash() {
        assertEquals(List.of("foo", "'bar/baz'", "boingo"), KPaths.of("/foo/'bar/baz'/boingo"));
        assertEquals(List.of("'a/b'"), KPaths.of("'a/b'"));
    }

    @Test
    void doubledQuoteIsEscapedQuote() {
        assertEquals(List.of("'it''s'", "x"), KPaths.of("'it''s'/x"));
        assertEquals(List.of("''''"), KPaths.of("''''"));
        assertEquals(List.of("''", "x"), KPaths.of("''/x"));
    }

    @Test
    void quoteInsideUnquotedStepIsLiteral() {
        assertEquals(List.of("it's", "x"), KPaths.of("it's/x"));
    }

    @Test
    void rejectsUnclosedQuote() {
        assertThrows(IllegalArgumentException.class, () -> KPaths.of("a/'b/c"));
        assertThrows(IllegalArgumentException.class, () -> KPaths.of("'it''s"));
    }

    @Test
    void rejectsTextAfterClosingQuote() {
        assertThrows(IllegalArgumentException.class, () -> KPaths.of("'a'b/c"));
    }

    @Test
    void quoteRoundTrips() {
        for (String key : List.of("plain", "bar/baz", "it's", "''", "*", "..", "[0]", "$x", "")) {
            String quoted = KPaths.quote(key);
            assertEquals(List.of(quoted), KPaths.of(quoted));
            assertEquals(key, KPaths.unquote(quoted));
            assertEquals(Step.Kind.Map1, new Step(quoted).kind());
            assertEquals(key, new Step(quoted).key());
        }
    }

    @Test
    void quotedStepsResolveLiteralKeys() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("boingo", "found");
        Map<String, Object> special = new LinkedHashMap<>();
        special.put("bar/baz", inner);
        special.put("*", "star");
        special.put("..", "dots");
        special.put("[0]", "bracket");
        special.put("$x", "dollar");
        special.put("it's", "quote");
        Map<String, Object> root = Map.of("foo", special);
        PathResolver resolver = PathResolver.create();

        List<Result> results = resolver.resolveAll(root, KPaths.of("/foo/'bar/baz'/boingo"));
        assertEquals(1, results.size());
        assertEquals("found", results.get(0).value());
        // the path records the literal key, not the quoted step
        assertEquals(List.of("foo", "bar/baz", "boingo"), results.get(0).path());

        assertEquals("star", resolver.resolveAll(root, KPaths.of("foo/'*'")).get(0).value());
        assertEquals("dots", resolver.resolveAll(root, KPaths.of("foo/'..'")).get(0).value());
        assertEquals("bracket", resolver.resolveAll(root, KPaths.of("foo/'[0]'")).get(0).value());
        assertEquals("dollar", resolver.resolveAll(root, KPaths.of("foo/'$x'")).get(0).value());
        assertEquals("quote", resolver.resolveAll(root, KPaths.of("foo/'it''s'")).get(0).value());
        // without quotes, '*' is still the wildcard
        assertEquals(6, resolver.resolveAll(root, KPaths.of("foo/*")).size());
    }

    @Test
    void quotedStepsWorkWithoutKPaths() {
        Map<String, Object> root = Map.of("*", "star", "a", "x");
        List<Result> results = PathResolver.create().resolveAll(root, List.of("'*'"));
        assertEquals(1, results.size());
        assertEquals("star", results.get(0).value());
    }

}
