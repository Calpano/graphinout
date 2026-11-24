package com.graphinout.foundation.jvm.kpath;

import com.graphinout.foundation.pure.collections.IMapLike;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CycleAndAdapterTest {

    interface Named {}

    interface Labeled {}

    interface NamedThing extends Named {}

    static class Both implements Named, Labeled {}

    static class Specific implements NamedThing {}

    private static List<List<Object>> paths(List<Result> results) {
        return results.stream().map(Result::path).toList();
    }

    @Test
    void anyCutsSelfReference() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("self", root);
        root.put("x", 1);
        assertEquals(List.of(List.of(), List.of("x")), paths(PathResolver.create().resolveAll(root, List.of(".."))));
    }

    @Test
    void anyCutsIndirectCycle() {
        Map<String, Object> a = new LinkedHashMap<>();
        Map<String, Object> b = new LinkedHashMap<>();
        a.put("b", b);
        b.put("a", a);
        b.put("y", "leaf");
        assertEquals(List.of(List.of(), List.of("b"), List.of("b", "y")), //
                paths(PathResolver.create().resolveAll(a, List.of(".."))));
    }

    @Test
    void anyReportsSharedValueOncePerPath() {
        List<String> shared = List.of("s");
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("p", shared);
        root.put("q", shared);
        List<Result> results = PathResolver.create().resolveAll(root, KPaths.of("../[0]"));
        assertEquals(List.of(List.of("p", "[0]"), List.of("q", "[0]")), paths(results));
    }

    @Test
    void unrelatedInterfaceAdaptersAreAmbiguous() {
        PathResolver resolver = PathResolver.create() //
                .registerMap(Named.class, v -> IMapLike.ofProperty("from", "Named")) //
                .registerMap(Labeled.class, v -> IMapLike.ofProperty("from", "Labeled"));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> resolver.resolveAll(new Both(), List.of("from")));
        assertTrue(e.getMessage().contains(Named.class.getName()), e.getMessage());
        assertTrue(e.getMessage().contains(Labeled.class.getName()), e.getMessage());
    }

    @Test
    void classAdapterResolvesAmbiguity() {
        PathResolver resolver = PathResolver.create() //
                .registerMap(Named.class, v -> IMapLike.ofProperty("from", "Named")) //
                .registerMap(Labeled.class, v -> IMapLike.ofProperty("from", "Labeled")) //
                .registerMap(Both.class, v -> IMapLike.ofProperty("from", "Both"));
        assertEquals("Both", resolver.resolveAll(new Both(), List.of("from")).get(0).value());
    }

    @Test
    void subInterfaceWinsOverItsSuperInterface() {
        PathResolver resolver = PathResolver.create() //
                .registerMap(Named.class, v -> IMapLike.ofProperty("from", "Named")) //
                .registerMap(NamedThing.class, v -> IMapLike.ofProperty("from", "NamedThing"));
        assertEquals("NamedThing", resolver.resolveAll(new Specific(), List.of("from")).get(0).value());
    }

}
