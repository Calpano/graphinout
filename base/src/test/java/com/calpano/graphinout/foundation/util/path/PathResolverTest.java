package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class PathResolverTest {

    public String foo(int bar) {
        return "foo-" + bar;
    }

    @Test
    void test() {
        PathResolver resolver = new PathResolver();

        resolver.registerList(PathResolverTest.class, value -> IListLike.of(3, value::foo));

        List<Result> res = resolver.resolveAll(this, List.of("[*]"));
        assertThat(res).isNotNull();
        assertThat(res).hasSize(3);
    }

    @Test
    void test2() throws IOException {
        String json = """
                { "a1": [
                    {"b": "a1-b"},
                    {"c": "a1-c"}
                  ],
                  "a2": [
                    {"b": "a2-b"},
                    {"c": "a2-c"}
                  ]
                }
                """;
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        assertThat(resolver.resolveAll(jsonValue, List.of("*"))).hasSize(2);
        assertThat(resolver.resolveAll(jsonValue, List.of("a1"))).hasSize(1);
        assertThat(resolver.resolveAll(jsonValue, List.of("[*]"))).isEmpty();

        assertThat(resolver.resolveAll(jsonValue, List.of("a1", "[*]"))).hasSize(2);
        assertThat(resolver.resolveAll(jsonValue, List.of("a1", "[*]", "b"))).hasSize(1);
        assertThat(resolver.resolveAll(jsonValue, List.of("*", "[*]", "b"))).hasSize(2);
        assertThat(resolver.resolveAll(jsonValue, List.of("*", "[*]", "b"))).hasSize(2);

        // there are 2 array in the tree
        List<Result> any = resolver.resolveAll(jsonValue, List.of(".."));
        assertThat(any).hasSize(11);
        assertThat(resolver.resolveAll(jsonValue, List.of("..", "[1]"))).hasSize(2);
    }

    @Test
    void testTypes() {
        assertThat(TypeAdapters.allInterfacesOf(JavaJsonObject.class)).contains(IJsonObject.class);
    }

    @Test
    void testEmptyObject() throws IOException {
        String json = "{}";
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        assertThat(resolver.resolveAll(jsonValue, List.of("*"))).isEmpty();
        assertThat(resolver.resolveAll(jsonValue, List.of("a"))).isEmpty();
    }

    @Test
    void testEmptyList() throws IOException {
        String json = "[]";
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        assertThat(resolver.resolveAll(jsonValue, List.of("[*]"))).isEmpty();
        assertThat(resolver.resolveAll(jsonValue, List.of("[0]"))).isEmpty();
    }

    @Test
    void testIndexOutOfBounds() throws IOException {
        String json = "[1, 2, 3]";
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        assertThat(resolver.resolveAll(jsonValue, List.of("[5]"))).isEmpty();
    }

    @Test
    void testNonExistentPath() throws IOException {
        String json = """
                { "a": { "b": "c" } }
                """;
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        assertThat(resolver.resolveAll(jsonValue, List.of("a", "x", "y"))).isEmpty();
    }

    @Test
    void testDeeplyNestedStructure() throws IOException {
        String json = """
                { "a": { "b": [ { "c": { "d": "found me" } } ] } }
                """;
        IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(json);
        PathResolver resolver = new PathResolver();
        List<Result> results = resolver.resolveAll(jsonValue, List.of("a", "b", "[0]", "c", "d"));
        assertThat(results).hasSize(1);
        Object value = results.getFirst().value();
        assert value instanceof IJsonPrimitive;
        IJsonPrimitive jsonPrimitive = (IJsonPrimitive) value;
        assertThat(jsonPrimitive.asString()).isEqualTo("found me");
    }

    @Test
    void testCustomObjectWithoutAdapter() {
        class CustomObject {}
        PathResolver resolver = new PathResolver();
        List<Result> results = resolver.resolveAll(new CustomObject(), List.of("a"));
        assertThat(results).isEmpty();
    }

}
