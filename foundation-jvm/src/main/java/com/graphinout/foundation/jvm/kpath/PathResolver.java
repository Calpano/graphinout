package com.graphinout.foundation.jvm.kpath;

import com.graphinout.foundation.pure.collections.IListLike;
import com.graphinout.foundation.pure.collections.IMapLike;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import com.graphinout.foundation.pure.types.ITypeAdapter;
import com.graphinout.foundation.pure.types.ITypeAdapters;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility for resolving paths within hierarchical data structures.
 * <p>
 * This class allows navigating object graphs using a simple path language. It can be extended to support custom data
 * types by registering {@link ITypeAdapter}s that convert them into {@link IMapLike} or {@link IListLike} structures.
 * <p>
 * By default, it supports {@link java.util.Map}, {@link java.util.List}, {@link IJsonObject}, and {@link IJsonArray}.
 */
@SuppressWarnings("UnusedReturnValue")
public class PathResolver {

    private final ITypeAdapters adapters = new TypeAdapters();

    /**
     * Constructs a new PathResolver and registers default adapters for common Java and JSON types.
     */
    public PathResolver() {
        // add trivial adapters
        registerList(List.class, IListLike::ofList);
        registerMap(Map.class, IMapLike::ofMap);
        // add JSON adapters
        registerMap(IJsonObject.class, IJsonValue::asMapLike);
        registerList(IJsonArray.class, IJsonValue::asListLike);
    }

    /**
     * Factory method to create a new {@link PathResolver} instance.
     *
     * @return a new {@link PathResolver}.
     */
    public static PathResolver create() {
        return new PathResolver();
    }

    /**
     * @return the direct children of {@code value}, relative to it: one step (key or {@code [i]}) per result. A value
     * that is both map-like and list-like contributes both kinds of children.
     */
    static List<Result> resolveDirectChildren(ITypeAdapters typeAdapters, Object value) {
        List<Result> results = new ArrayList<>();
        @Nullable ITypeAdapter<Object, IMapLike> mapAdapter = typeAdapters.findAdapterFromTo(value.getClass(), IMapLike.class, false);
        if (mapAdapter != null) {
            results.addAll(Step.mapAll(mapAdapter.toAdapted(value)));
        }
        @Nullable ITypeAdapter<Object, IListLike> listAdapter = typeAdapters.findAdapterFromTo(value.getClass(), IListLike.class, false);
        if (listAdapter != null) {
            results.addAll(Step.listAll(listAdapter.toAdapted(value)));
        }
        return results;
    }

    /** @return true if the value of result is (by identity) also one of its ancestors */
    private static boolean isOnPathTo(Result result) {
        List<Object> values = result.values();
        Object value = values.get(values.size() - 1);
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) == value) return true;
        }
        return false;
    }

    /**
     * The {@code ..} step: {@code from} itself, then all its descendants, breadth-first. Each descendant's result
     * extends {@code from} by the actual keys and indexes taken, and by every value passed on the way down.
     * <p>
     * Cycles are cut: a value that is (by identity) already on its own path is not visited again. A value reachable
     * by several paths that don't form a cycle is reported once per path.
     *
     * @param from where to start
     * @return {@code from} followed by one result per descendant
     */
    List<Result> descendantsOrSelf(Result from) {
        List<Result> all = new ArrayList<>();
        all.add(from);
        List<Result> current = List.of(from);
        while (!current.isEmpty()) {
            List<Result> next = new ArrayList<>();
            for (Result result : current) {
                for (Result child : Result.concatResults(result, resolveDirectChildren(typeAdapters(), result.value()))) {
                    // a value that is already on its own path is a cycle: don't visit it again
                    if (!isOnPathTo(child)) next.add(child);
                }
            }
            all.addAll(next);
            current = next;
        }
        return all;
    }

    /**
     * Registers a type adapter to treat instances of a given class as a list-like structure.
     *
     * @param type    the class to be adapted.
     * @param adapter the adapter that converts an instance of {@code T} to an {@link IListLike}.
     * @param <T>     the type of the object to be adapted.
     * @return this {@link PathResolver} instance for chaining.
     */
    public <T> PathResolver registerList(Class<T> type, ITypeAdapter<T, IListLike> adapter) {
        adapters.register(type, IListLike.class, adapter);
        return this;
    }

    /**
     * Registers a type adapter to treat instances of a given class as a map-like structure.
     *
     * @param type    the class to be adapted.
     * @param adapter the adapter that converts an instance of {@code T} to an {@link IMapLike}.
     * @param <T>     the type of the object to be adapted.
     * @return this {@link PathResolver} instance for chaining.
     */
    public <T> PathResolver registerMap(Class<T> type, ITypeAdapter<T, IMapLike> adapter) {
        adapters.register(type, IMapLike.class, adapter);
        return this;
    }

    /**
     * Resolves a single path step against a root object.
     * <p>
     * This may result in multiple results if the step is a wildcard (e.g., "*"). Same as
     * {@code resolveAll(root, List.of(step))}: each result's values start with {@code root}.
     *
     * @param root the object from which to start resolution.
     * @param step the path step to resolve.
     * @return a list of {@link Result} objects matching the step.
     * @throws IllegalArgumentException if the step is not valid, e.g. {@code [x]}
     */
    public List<Result> resolve1(Object root, String step) {
        return new Step(step).resolve(this, Result.ofRoot(root));
    }

    /**
     * Resolves a single path step against a previous result.
     * <p>
     * This may result in multiple results if the step is a wildcard. The path of the new results will be a
     * concatenation of the root's path and the new step.
     *
     * @param root the starting {@link Result}.
     * @param step the path step to resolve.
     * @return a list of {@link Result} objects.
     * @throws IllegalArgumentException if the step is not valid, e.g. {@code [x]}
     */
    public List<Result> resolve1(Result root, String step) {
        return resolveStep(root, new Step(step));
    }

    /**
     * Resolves a single path step against a list of root results and returns a flattened list of new results.
     *
     * @param roots the list of starting {@link Result}s.
     * @param step  the path step to resolve.
     * @return a flattened list of all resolved {@link Result} objects.
     * @throws IllegalArgumentException if the step is not valid, e.g. {@code [x]}, even if {@code roots} is empty
     */
    public List<Result> resolve1(List<Result> roots, String step) {
        return resolveStep(roots, new Step(step));
    }

    /**
     * Resolves a multi-step path against a root object.
     * <p>
     * The path syntax supports property names for map-like objects and bracketed indices for list-like objects. See
     * {@link Step} for more details.
     *
     * @param root at which to resolve the path.
     * @param path a list of strings representing the path steps.
     * @return a list of all matching {@link Result} objects; may be empty.
     * @throws IllegalArgumentException if any step is not valid, e.g. {@code [x]}
     */
    public List<Result> resolveAll(Object root, List<String> path) {
        List<Result> roots = List.of(Result.ofRoot(root));
        return resolveAll(roots, path);
    }

    /**
     * Resolves a multi-step path against a list of starting results.
     *
     * @param roots the list of starting {@link Result}s.
     * @param path  a list of strings representing the path steps.
     * @return a list of all matching {@link Result} objects; may be empty.
     * @throws IllegalArgumentException if any step is not valid, e.g. {@code [x]}; all steps are checked before
     *                                  resolving, so this does not depend on the data
     */
    public List<Result> resolveAll(List<Result> roots, List<String> path) {
        // parse all steps first, so invalid steps fail regardless of whether data reaches them
        List<Step> steps = new ArrayList<>(path.size());
        for (String step : path) {
            steps.add(new Step(step));
        }
        List<Result> current = roots;
        for (Step step : steps) {
            current = resolveStep(current, step);
        }
        return current;
    }

    private List<Result> resolveStep(Result root, Step step) {
        return step.resolve(this, root);
    }

    private List<Result> resolveStep(List<Result> roots, Step step) {
        List<Result> results = new ArrayList<>();
        for (Result root : roots) {
            results.addAll(resolveStep(root, step));
        }
        return results;
    }

    /**
     * Resolves the {@code ..} step: the root itself, then all its descendants, breadth-first.
     *
     * @param root the object from which to start resolution.
     * @return the root's result (empty path) followed by one result per descendant, with the full path to it.
     */
    public List<Result> resolveAny(Object root) {
        return resolve1(root, "..");
    }

    /**
     * Gets the {@link TypeAdapters} instance used by this resolver.
     *
     * @return the {@link TypeAdapters} instance.
     */
    public ITypeAdapters typeAdapters() {
        return adapters;
    }

}
