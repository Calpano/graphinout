package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MagicMutableJsonValueTest {

    IJsonFactory factory = JavaJsonFactory.INSTANCE;
    MagicMutableJsonValue underTest;

    @Test
    void addProperty_to_array_should_throw() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.addMerge(factory.createInteger(1));
        underTest.addMerge(factory.createInteger(2));
        assertThrows(IllegalStateException.class, () -> underTest.addProperty(List.of("a"), factory.createString("b")));
    }

    @Test
    void addProperty_to_empty_should_create_object() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.addProperty(List.of("a", "b"), factory.createString("c"));
        assertEquals("""
                {"a":{"b":"c"}}""", write(underTest));
    }

    @Test
    void addProperty_to_empty_should_create_object__simple() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.addProperty("foo", "bar");
        assertEquals("""
                {"foo":"bar"}""", write(underTest));
    }

    @Test
    void addProperty_to_object_should_add_property() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.addProperty(List.of("x"), factory.createInteger(1));
        underTest.addProperty(List.of("y"), factory.createString("z"));
        assertEquals("""
                {"x":1,"y":"z"}""", write(underTest));
    }

    @Test
    void addProperty_to_primitive_should_wrap_in_object() {
        underTest = new MagicMutableJsonValue(factory, factory.createString("initial"));
        underTest.addProperty(List.of("a"), factory.createString("b"));
        assertEquals("""
                {"a":"b","value":"initial"}""", write(underTest));
    }

    @Test
    void append_with_path_should_behave_like_addProperty() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.append(List.of("a", "b"), factory.createString("c"));
        assertEquals("""
                {"a":{"b":"c"}}""", write(underTest));
    }

    @Test
    void append_without_path_to_array_should_append() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.append(Collections.emptyList(), factory.createString("a"));
        assertEquals("\"a\"", write(underTest));
        underTest.append(Collections.emptyList(), factory.createString("b"));
        assertEquals("""
                ["a","b"]""", write(underTest));
    }

    @Test
    void append_without_path_to_object_should_throw() {
        underTest = new MagicMutableJsonValue(factory, null);
        underTest.append(List.of("x"), factory.createInteger(1));
        assertThrows(IllegalStateException.class, () -> underTest.append(Collections.emptyList(), factory.createString("b")));
    }

    @Test
    void append_without_path_to_primitive_should_create_array() {
        underTest = new MagicMutableJsonValue(factory, factory.createString("a"));
        underTest.append(Collections.emptyList(), factory.createString("b"));
        assertEquals("""
                ["a","b"]""", write(underTest));
    }

    private String write(IJsonValue value) throws JsonException {
        StringBuilderJsonWriter writer = new StringBuilderJsonWriter();
        writer.documentStart();
        value.fire(writer);
        writer.documentEnd();
        return writer.json();
    }

}
