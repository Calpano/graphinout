package com.graphinout.foundation.jvm.kpath;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The {@code ..} step (descendant-or-self) records the full path to each match. */
class AnyStepTest {

    /** {"x": ["a", "b"], "y": {"z": 1}} */
    private static Map<String, Object> data() {
        Map<String, Object> y = new LinkedHashMap<>();
        y.put("z", 1);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("x", List.of("a", "b"));
        root.put("y", y);
        return root;
    }

    private static Result only(List<Result> results) {
        assertEquals(1, results.size(), () -> "results: " + results);
        return results.get(0);
    }

    @Test
    void anyRecordsActualPathsBreadthFirst() {
        Map<String, Object> root = data();
        List<Result> results = PathResolver.create().resolveAll(root, List.of(".."));
        assertEquals(List.of(
                List.of(),
                List.of("x"),
                List.of("y"),
                List.of("x", "[0]"),
                List.of("x", "[1]"),
                List.of("y", "z")
        ), results.stream().map(Result::path).toList());
        for (Result r : results) {
            // values = root + one value per step
            assertEquals(r.path().size() + 1, r.values().size(), () -> "result " + r.path());
            assertEquals(root, r.values().get(0));
        }
    }

    @Test
    void anySelfMatchAddsNoStep() {
        Map<String, Object> root = data();
        Result self = PathResolver.create().resolveAll(root, List.of("..")).get(0);
        assertEquals(List.of(), self.path());
        assertEquals(List.of(root), self.values());
        assertEquals(root, self.value());
    }

    @Test
    void stepsAfterAnyContinueTheFullPath() {
        Map<String, Object> root = data();
        Result r = only(PathResolver.create().resolveAll(root, KPaths.of("../[1]")));
        assertEquals("b", r.value());
        assertEquals(List.of("x", "[1]"), r.path());
        assertEquals(List.of(root, List.of("a", "b"), "b"), r.values());
    }

    @Test
    void anyInTheMiddleOfAPath() {
        Map<String, Object> root = data();
        Result r = only(PathResolver.create().resolveAll(root, KPaths.of("y/../z")));
        assertEquals(1, r.value());
        assertEquals(List.of("y", "z"), r.path());
        assertEquals(3, r.values().size());
    }

    @Test
    void resolveAnyIncludesRoot() {
        Map<String, Object> root = data();
        List<Result> results = PathResolver.create().resolveAny(root);
        assertEquals(6, results.size());
        assertEquals(List.of(root), results.get(0).values());
        assertEquals(List.of(root, List.of("a", "b"), "a"), results.get(3).values());
    }

    @Test
    void resolve1IncludesRootLikeResolveAll() {
        Map<String, Object> root = data();
        PathResolver resolver = PathResolver.create();
        assertEquals(resolver.resolveAll(root, List.of("x")), resolver.resolve1(root, "x"));
        assertEquals(List.of(root, List.of("a", "b")), resolver.resolve1(root, "x").get(0).values());
    }

}
