package com.graphinout.foundation.pure.json.query;

import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.pure.json.query.JsonQueryEngine.matches;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonQueryEngineTest {

    private static final IJsonFactory F = JavaJsonFactory.INSTANCE;

    // ------------------------------------------------------------------
    // Helpers: build JSON values concisely
    // ------------------------------------------------------------------

    /** {"key": "value"} */
    private static IJsonObject obj(String key, IJsonValue value) {
        return F.createObjectMutable().addProperty(key, value);
    }

    /** {"k1": v1, "k2": v2} */
    private static IJsonObject obj(String k1, IJsonValue v1, String k2, IJsonValue v2) {
        return F.createObjectMutable().addProperty(k1, v1).addProperty(k2, v2);
    }

    /** [elements…] */
    private static IJsonArrayMutable arr(IJsonValue... elements) {
        IJsonArrayMutable a = F.createArrayMutable();
        for (IJsonValue e : elements) a.add(e);
        return a;
    }

    // ------------------------------------------------------------------
    // Root path "/"
    // ------------------------------------------------------------------

    @Test
    void rootPath_present_matches() {
        IJsonValue root = F.createString("hello");
        assertThat(matches(JsonQuery.of("/", IJsonExpect.expectPresent()), root)).isTrue();
    }

    @Test
    void rootPath_expectType_string_matches() {
        assertThat(matches(JsonQuery.of("/", IJsonExpect.expectType(JsonType.String)), F.createString("x"))).isTrue();
    }

    @Test
    void rootPath_expectType_string_wrongType_fails() {
        assertThat(matches(JsonQuery.of("/", IJsonExpect.expectType(JsonType.String)), F.createInteger(1))).isFalse();
    }

    // ------------------------------------------------------------------
    // Simple property navigation
    // ------------------------------------------------------------------

    @Test
    void simpleProperty_present() {
        IJsonValue doc = obj("name", F.createString("Alice"));
        assertThat(matches(JsonQuery.of("/name", IJsonExpect.expectPresent()), doc)).isTrue();
    }

    @Test
    void simpleProperty_absent() {
        IJsonValue doc = obj("name", F.createString("Alice"));
        assertThat(matches(JsonQuery.of("/missing", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    @Test
    void simpleProperty_absent_expectAbsent() {
        IJsonValue doc = obj("name", F.createString("Alice"));
        assertThat(matches(JsonQuery.of("/missing", IJsonExpect.expectAbsent()), doc)).isTrue();
    }

    @Test
    void simpleProperty_present_expectAbsent_fails() {
        IJsonValue doc = obj("name", F.createString("Alice"));
        assertThat(matches(JsonQuery.of("/name", IJsonExpect.expectAbsent()), doc)).isFalse();
    }

    @Test
    void simpleProperty_stringValue_matches() {
        IJsonValue doc = obj("type", F.createString("node"));
        assertThat(matches(JsonQuery.requireString("/type", "node"), doc)).isTrue();
    }

    @Test
    void simpleProperty_stringValue_wrong_fails() {
        IJsonValue doc = obj("type", F.createString("edge"));
        assertThat(matches(JsonQuery.requireString("/type", "node"), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // Nested navigation
    // ------------------------------------------------------------------

    @Test
    void nestedProperty_matches() {
        // {"a": {"b": "deep"}}
        IJsonValue doc = obj("a", obj("b", F.createString("deep")));
        assertThat(matches(JsonQuery.of("/a/b", IJsonExpect.expectPresent()), doc)).isTrue();
    }

    @Test
    void nestedProperty_middleMissing_fails() {
        IJsonValue doc = obj("a", F.createString("not-an-object"));
        assertThat(matches(JsonQuery.of("/a/b", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    @Test
    void nestedProperty_leafMissing() {
        IJsonValue doc = obj("a", obj("x", F.createString("y")));
        assertThat(matches(JsonQuery.of("/a/b", IJsonExpect.expectAbsent()), doc)).isTrue();
    }

    // ------------------------------------------------------------------
    // Array index navigation
    // ------------------------------------------------------------------

    @Test
    void arrayIndex_valid_matches() {
        // {"items": ["a", "b", "c"]}
        IJsonValue doc = obj("items", arr(F.createString("a"), F.createString("b"), F.createString("c")));
        assertThat(matches(JsonQuery.requireString("/items/1", "b"), doc)).isTrue();
    }

    @Test
    void arrayIndex_outOfBounds_absent() {
        IJsonValue doc = obj("items", arr(F.createString("only")));
        assertThat(matches(JsonQuery.of("/items/5", IJsonExpect.expectAbsent()), doc)).isTrue();
    }

    @Test
    void arrayIndex_outOfBounds_notPresent() {
        IJsonValue doc = obj("items", arr(F.createString("only")));
        assertThat(matches(JsonQuery.of("/items/5", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    @Test
    void arrayIndex_zero_matches() {
        IJsonValue doc = arr(F.createString("first"), F.createString("second"));
        assertThat(matches(JsonQuery.requireString("/0", "first"), doc)).isTrue();
    }

    // ------------------------------------------------------------------
    // Wildcard on objects
    // ------------------------------------------------------------------

    @Test
    void wildcardObject_anyChildPresent_matches() {
        // {"a": "1", "b": "2"} — at least one child is a string
        IJsonValue doc = obj("a", F.createString("1"), "b", F.createString("2"));
        assertThat(matches(JsonQuery.of("/*/", IJsonExpect.expectPresent()), doc)).isFalse(); // trailing slash = extra segment
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectPresent()), doc)).isTrue();
    }

    @Test
    void wildcardObject_specificValueFound() {
        // {"x": "wrong", "y": "target"}
        IJsonValue doc = obj("x", F.createString("wrong"), "y", F.createString("target"));
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectStringValue("target")), doc)).isTrue();
    }

    @Test
    void wildcardObject_noChildMatchesValue_fails() {
        IJsonValue doc = obj("x", F.createString("a"), "y", F.createString("b"));
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectStringValue("missing")), doc)).isFalse();
    }

    @Test
    void wildcardObject_emptyObject_fails() {
        IJsonValue doc = F.createObjectMutable();
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // Wildcard on arrays
    // ------------------------------------------------------------------

    @Test
    void wildcardArray_anyElement_matches() {
        IJsonValue doc = arr(F.createString("a"), F.createString("b"));
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectPresent()), doc)).isTrue();
    }

    @Test
    void wildcardArray_specificValueFound() {
        IJsonValue doc = arr(F.createString("x"), F.createString("needle"), F.createString("z"));
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectStringValue("needle")), doc)).isTrue();
    }

    @Test
    void wildcardArray_emptyArray_fails() {
        IJsonValue doc = F.createArrayMutable();
        assertThat(matches(JsonQuery.of("/*", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // Deep wildcard — /foo/*/bar
    // ------------------------------------------------------------------

    @Test
    void deepWildcard_objectChildren_findsBar() {
        // {"items": {"a": {"bar": "yes"}, "b": {"bar": "no"}}}
        IJsonObject itemA = obj("bar", F.createString("yes"));
        IJsonObject itemB = obj("bar", F.createString("no"));
        IJsonValue doc = obj("items", obj("a", itemA, "b", itemB));
        assertThat(matches(JsonQuery.requireString("/items/*/bar", "yes"), doc)).isTrue();
    }

    @Test
    void deepWildcard_arrayChildren_findsProperty() {
        // [{"type": "node"}, {"type": "edge"}]
        IJsonValue doc = arr(obj("type", F.createString("node")), obj("type", F.createString("edge")));
        assertThat(matches(JsonQuery.requireString("/*/type", "node"), doc)).isTrue();
    }

    @Test
    void deepWildcard_noChildHasProperty_fails() {
        IJsonValue doc = arr(obj("x", F.createString("1")), obj("x", F.createString("2")));
        assertThat(matches(JsonQuery.of("/*/type", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // Navigating through primitive (wrong container)
    // ------------------------------------------------------------------

    @Test
    void navigateThroughPrimitive_treatedAsAbsent() {
        // "hello" is a primitive — cannot navigate into /foo
        IJsonValue doc = F.createString("hello");
        assertThat(matches(JsonQuery.of("/foo", IJsonExpect.expectAbsent()), doc)).isTrue();
        assertThat(matches(JsonQuery.of("/foo", IJsonExpect.expectPresent()), doc)).isFalse();
    }

    @Test
    void navigateThroughArray_withObjectKey_treatedAsAbsent() {
        // arr[0] is a string; trying to navigate "/0/foo" hits a primitive at /0
        IJsonValue doc = arr(F.createString("not-an-object"));
        assertThat(matches(JsonQuery.of("/0/foo", IJsonExpect.expectAbsent()), doc)).isTrue();
    }

    // ------------------------------------------------------------------
    // JSON null entries (present but null-typed)
    // ------------------------------------------------------------------

    @Test
    void jsonNull_isPresent() {
        IJsonValue doc = obj("key", F.createNull());
        // value IS present; it just has JSON type Null
        assertThat(matches(JsonQuery.of("/key", IJsonExpect.expectPresent()), doc)).isTrue();
    }

    @Test
    void jsonNull_hasTypeNull() {
        IJsonValue doc = obj("key", F.createNull());
        assertThat(matches(JsonQuery.of("/key", IJsonExpect.expectType(JsonType.Null)), doc)).isTrue();
    }

    @Test
    void jsonNull_isNotAbsent() {
        IJsonValue doc = obj("key", F.createNull());
        assertThat(matches(JsonQuery.of("/key", IJsonExpect.expectAbsent()), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // expectValue — identity equality against a specific IJsonValue
    // ------------------------------------------------------------------

    @Test
    void expectValue_sameInstance_matches() {
        // expectValue uses IJsonValue.equals(); JavaJsonFactory is reference-equal,
        // so the same instance stored and retrieved from an object matches itself.
        IJsonValue needle = F.createString("exact");
        IJsonObjectMutable doc = F.createObjectMutable();
        doc.addProperty("v", needle);
        assertThat(matches(JsonQuery.of("/v", IJsonExpect.expectValue(needle)), doc)).isTrue();
    }

    @Test
    void expectValue_differentInstance_sameContent_matches() {
        IJsonValue needle = F.createString("exact");
        IJsonValue doc = obj("v", F.createString("exact"));
        assertThat(matches(JsonQuery.of("/v", IJsonExpect.expectValue(needle)), doc)).isTrue();
    }

    @Test
    void expectValue_absent_fails() {
        IJsonValue needle = F.createString("exact");
        IJsonValue doc = obj("x", F.createString("exact"));
        assertThat(matches(JsonQuery.of("/missing", IJsonExpect.expectValue(needle)), doc)).isFalse();
    }

    @Test
    void expectValue_wrongValue_fails() {
        IJsonValue needle = F.createString("exact");
        IJsonValue doc = obj("v", F.createString("other"));
        assertThat(matches(JsonQuery.of("/v", IJsonExpect.expectValue(needle)), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // IJsonExpect combinators
    // ------------------------------------------------------------------

    @Test
    void and_bothTrue_matches() {
        IJsonExpect bothString = IJsonExpect.expectType(JsonType.String).and(IJsonExpect.expectStringValue("hi"));
        IJsonValue doc = obj("v", F.createString("hi"));
        assertThat(matches(JsonQuery.of("/v", bothString), doc)).isTrue();
    }

    @Test
    void and_oneFails_fails() {
        IJsonExpect bothString = IJsonExpect.expectType(JsonType.String).and(IJsonExpect.expectStringValue("other"));
        IJsonValue doc = obj("v", F.createString("hi"));
        assertThat(matches(JsonQuery.of("/v", bothString), doc)).isFalse();
    }

    @Test
    void or_firstTrue_matches() {
        IJsonExpect either = IJsonExpect.expectStringValue("a").or(IJsonExpect.expectStringValue("b"));
        assertThat(matches(JsonQuery.of("/v", either), obj("v", F.createString("a")))).isTrue();
        assertThat(matches(JsonQuery.of("/v", either), obj("v", F.createString("b")))).isTrue();
    }

    @Test
    void or_neitherTrue_fails() {
        IJsonExpect either = IJsonExpect.expectStringValue("a").or(IJsonExpect.expectStringValue("b"));
        assertThat(matches(JsonQuery.of("/v", either), obj("v", F.createString("c")))).isFalse();
    }

    @Test
    void negate_presenceFlipped() {
        IJsonExpect notPresent = IJsonExpect.expectPresent().negate();
        IJsonValue doc = obj("x", F.createString("y"));
        assertThat(matches(JsonQuery.of("/x", notPresent), doc)).isFalse();
        assertThat(matches(JsonQuery.of("/missing", notPresent), doc)).isTrue();
    }

    // ------------------------------------------------------------------
    // Multi-test query (all tests must pass)
    // ------------------------------------------------------------------

    @Test
    void multiTest_allPass_matches() {
        IJsonValue doc = obj("type", F.createString("node"), "id", F.createString("n1"));
        JsonQuery q = new JsonQuery(
                JsonPathCheck.of("/type", IJsonExpect.expectStringValue("node")),
                JsonPathCheck.of("/id", IJsonExpect.expectPresent())
        );
        assertThat(matches(q, doc)).isTrue();
    }

    @Test
    void multiTest_oneFails_fails() {
        IJsonValue doc = obj("type", F.createString("edge"), "id", F.createString("n1"));
        JsonQuery q = new JsonQuery(
                JsonPathCheck.of("/type", IJsonExpect.expectStringValue("node")),  // fails
                JsonPathCheck.of("/id", IJsonExpect.expectPresent())
        );
        assertThat(matches(q, doc)).isFalse();
    }

    @Test
    void multiTest_empty_alwaysMatches() {
        IJsonValue doc = F.createString("anything");
        assertThat(matches(new JsonQuery(), doc)).isTrue();
    }

    // ------------------------------------------------------------------
    // Factory convenience methods
    // ------------------------------------------------------------------

    @Test
    void requirePresent_factory() {
        IJsonValue doc = obj("x", F.createInteger(42));
        assertThat(matches(JsonQuery.requirePresent("/x"), doc)).isTrue();
        assertThat(matches(JsonQuery.requirePresent("/y"), doc)).isFalse();
    }

    @Test
    void requireString_factory() {
        IJsonValue doc = obj("color", F.createString("blue"));
        assertThat(matches(JsonQuery.requireString("/color", "blue"), doc)).isTrue();
        assertThat(matches(JsonQuery.requireString("/color", "red"), doc)).isFalse();
    }

    // ------------------------------------------------------------------
    // Invalid paths
    // ------------------------------------------------------------------

    @Test
    void pathWithoutLeadingSlash_throws() {
        IJsonValue doc = F.createString("x");
        assertThrows(IllegalArgumentException.class,
                () -> matches(JsonQuery.of("no-slash", IJsonExpect.expectPresent()), doc));
    }
}
