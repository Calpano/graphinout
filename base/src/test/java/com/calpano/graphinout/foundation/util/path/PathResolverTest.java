package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
                {
                    "a1": [
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

}
