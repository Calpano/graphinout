package com.graphinout.foundation.json.impl;

import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.graphinout.foundation.json.path.IJsonContainerNavigationStep.pathOf;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonMakerTest {

    IJsonFactory factory = JavaJsonFactory.INSTANCE;

    @Test
    void testCreateMulti() {
        IJsonValue value = JsonMaker.create(factory, pathOf("foo", "bar", "baz"), factory.createInteger(123));
        assertThat(value.toJsonString()).isEqualTo("""
                {"foo":{"bar":{"baz":123}}}""");
    }

    /**
     * Root: null, Adding: {"x":1}, Expect: {"x":1}
     */
    @Test
    void append_to_existing_object_should_add_property() {
        IJsonValue root = factory.createObjectMutable();
        List<IJsonContainerNavigationStep> path = pathOf("x");
        IJsonValue result = JsonMaker.append(factory, root, path, factory.createInteger(1));

        assertEquals("""
                {"x":1}""", result.toJsonString());
    }

    /**
     * Root: null, Adding: {"bbb":"ccc"}, Expect: {"bbb":"ccc"}
     */
    @Test
    void append_to_null_should_create_single_level_object() {
        List<IJsonContainerNavigationStep> path = pathOf("bbb");
        IJsonValue result = JsonMaker.append(factory, null, path, factory.createString("ccc"));

        assertThat(result).isNotNull();
        assertThat(result.isObject()).isTrue();
        assertEquals("""
                {"bbb":"ccc"}""", result.toJsonString());
    }

    /**
     * Root: null, Adding: "test" (empty path), Expect: "test"
     */
    @Test
    void append_to_null_with_empty_path_should_return_value() {
        IJsonValue value = factory.createString("test");
        IJsonValue result = JsonMaker.append(factory, null, Collections.emptyList(), value);

        assertEquals("\"test\"", result.toJsonString());
    }

    /**
     * Root: "initial", Adding: "second" (empty path), Expect: ["initial","second"]
     */
    @Test
    void append_to_primitive_with_empty_path_should_create_array() {
        IJsonValue root = factory.createString("initial");
        IJsonValue result = JsonMaker.append(factory, root, Collections.emptyList(), factory.createString("second"));

        assertEquals("""
                ["initial","second"]""", result.toJsonString());
    }

    /**
     * Root: none (create operation), Creating: [1] -> "item", Expect: IllegalArgumentException
     */
    @Test
    void create_with_array_path_non_zero_index_should_throw() {
        List<IJsonContainerNavigationStep> path = pathOf(1);
        assertThrows(IllegalArgumentException.class, () -> JsonMaker.create(factory, path, factory.createString("item")));
    }

    /**
     * Root: none (create operation), Creating: [0] -> "item", Expect: ["item"]
     */
    @Test
    void create_with_array_path_should_create_array() {
        List<IJsonContainerNavigationStep> path = pathOf(0);
        IJsonValue result = JsonMaker.create(factory, path, factory.createString("item"));

        assertEquals("""
                ["item"]""", result.toJsonString());
    }

    /**
     * Root: none (create operation), Creating: 42 (empty path), Expect: 42
     */
    @Test
    void create_with_empty_path_should_return_value() {
        IJsonValue value = factory.createInteger(42);
        IJsonValue result = JsonMaker.create(factory, Collections.emptyList(), value);

        assertEquals("42", result.toJsonString());
    }

    /**
     * Root: none (create operation), Creating: "b" -> "c", Expect: {"b":"c"}
     */
    @Test
    void create_with_object_path_should_create_single_level_object() {
        List<IJsonContainerNavigationStep> path = pathOf("b");
        IJsonValue result = JsonMaker.create(factory, path, factory.createString("c"));

        assertEquals("""
                {"b":"c"}""", result.toJsonString());
    }

    /**
     * Root: ["existing"], Merging: [0] -> "new", Expect: ["existing","new"]
     */
    @Test
    void merge_array_with_array_path_should_append() {
        IJsonValue root = factory.createArrayMutable();
        factory.asArrayMutable(root.asArray()).add(factory.createString("existing"));
        List<IJsonContainerNavigationStep> path = IJsonContainerNavigationStep.pathOf(0);

        IJsonValue result = JsonMaker.merge(factory, root, path, factory.createString("new"));

        assertEquals("""
                ["existing","new"]""", result.toJsonString());
    }

    /**
     * Root: ["existing"], Merging: "new" (empty path), Expect: ["existing","new"]
     */
    @Test
    void merge_array_with_empty_path_should_append() {
        IJsonValue root = factory.createArrayMutable();
        factory.asArrayMutable(root.asArray()).add(factory.createString("existing"));

        IJsonValue result = JsonMaker.merge(factory, root, Collections.emptyList(), factory.createString("new"));

        assertEquals("""
                ["existing","new"]""", result.toJsonString());
    }

    /**
     * Root: [] (array), Merging: "prop" -> "value", Expect: IllegalStateException
     */
    @Test
    void merge_array_with_object_path_should_throw() {
        IJsonValue root = factory.createArrayMutable();
        List<IJsonContainerNavigationStep> path = pathOf("prop");

        assertThrows(IllegalStateException.class, () -> JsonMaker.merge(factory, root, path, factory.createString("value")));
    }

    /**
     * Root: {} (object), Merging: [0] -> "value", Expect: IllegalStateException
     */
    @Test
    void merge_object_with_array_path_should_throw() {
        IJsonValue root = factory.createObjectMutable();
        List<IJsonContainerNavigationStep> path = pathOf(0);

        assertThrows(IllegalStateException.class, () -> JsonMaker.merge(factory, root, path, factory.createString("value")));
    }

    /**
     * Root: {"key":"value"}, Merging: "new" (empty path), Expect: IllegalStateException
     */
    @Test
    void merge_object_with_empty_path_should_throw() {
        IJsonValue root = factory.createObjectMutable();
        factory.asObjectMutable(root.asObject()).addProperty("key", factory.createString("value"));

        assertThrows(IllegalStateException.class, () -> JsonMaker.merge(factory, root, Collections.emptyList(), factory.createString("new")));
    }

    /**
     * Root: {} (empty object), Merging: "newProp" -> "value", Expect: {"newProp":"value"}
     */
    @Test
    void merge_object_with_object_path_should_add_property() {
        IJsonValue root = factory.createObjectMutable();
        List<IJsonContainerNavigationStep> path = pathOf("newProp");

        IJsonValue result = JsonMaker.merge(factory, root, path, factory.createString("value"));

        assertEquals("""
                {"newProp":"value"}""", result.toJsonString());
    }

    /**
     * Root: "primitive", Merging: [0] -> "value", Expect: ["primitive","value"]
     */
    @Test
    void merge_primitive_with_array_path_should_create_array() {
        IJsonValue root = factory.createString("primitive");
        List<IJsonContainerNavigationStep> path = pathOf(0);

        IJsonValue result = JsonMaker.merge(factory, root, path, factory.createString("value"));

        assertEquals("""
                ["primitive","value"]""", result.toJsonString());
    }

    /**
     * Root: "first", Merging: "second" (empty path), Expect: ["first","second"]
     */
    @Test
    void merge_primitive_with_empty_path_should_create_array() {
        IJsonValue root = factory.createString("first");
        IJsonValue value = factory.createString("second");
        IJsonValue result = JsonMaker.merge(factory, root, Collections.emptyList(), value);

        assertEquals("""
                ["first","second"]""", result.toJsonString());
    }

    /**
     * Root: "primitive", Merging: "prop" -> "value", Expect: IllegalStateException
     */
    @Test
    void merge_primitive_with_object_path_should_throw() {
        IJsonValue root = factory.createString("primitive");
        List<IJsonContainerNavigationStep> path = pathOf("prop");

        assertThrows(IllegalStateException.class, () -> JsonMaker.merge(factory, root, path, factory.createString("value")));
    }

    /**
     * Root: null, Adding: "x" -> 1 then "y" -> "z", Expect: {"x":1,"y":"z"}
     */
    @Test
    void multiple_operations_scenario() {
        // Demonstrate multiple JsonMaker operations similar to MagicMutableJsonValue patterns
        IJsonValue root = null;

        // Create first property x = 1
        root = JsonMaker.append(factory, root, IJsonContainerNavigationStep.pathOf("x"), factory.createInteger(1));

        // Add second property y = "z"
        root = JsonMaker.append(factory, root, IJsonContainerNavigationStep.pathOf("y"), factory.createString("z"));

        assertEquals("""
                {"x":1,"y":"z"}""", root.toJsonString());

        // Verify structure similar to MagicMutableJsonValueTest
        assertThat(root.isObject()).isTrue();
        assertThat(root.asObject().get("x")).isNotNull();
        assertThat(root.asObject().get("y")).isNotNull();
        assertThat(root.asObject().get_("x").toJsonString()).isEqualTo("1");
        assertThat(root.asObject().get_("y").toJsonString()).isEqualTo("\"z\"");
    }

}
