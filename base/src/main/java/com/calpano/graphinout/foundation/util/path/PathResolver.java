package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility for resolving paths within hierarchical data structures.
 * <p>
 * This class allows navigating object graphs using a simple path language. It can be extended to support custom data types
 * by registering {@link ITypeAdapter}s that convert them into {@link IMapLike} or {@link IListLike} structures.
 * <p>
 * By default, it supports {@link java.util.Map}, {@link java.util.List}, {@link IJsonObject}, and {@link IJsonArray}.
 */
@SuppressWarnings("UnusedReturnValue")
public class PathResolver {

    private final TypeAdapters adapters = new TypeAdapters();

    /**
     * Constructs a new PathResolver and registers default adapters for common Java and JSON types.
     */
    public PathResolver() {
        // add trivial adapters
        registerList(List.class, IListLike::ofList);
        registerMap(Map.class, IMapLike::ofMap);
        registerMap(IJsonObject.class, jo -> IMapLike.of(() -> jo.keys().stream().sorted().toList(), jo::get));
        registerList(IJsonArray.class, ja -> IListLike.of(ja::size, ja::get));
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
     * This may result in multiple results if the step is a wildcard (e.g., "*").
     *
     * @param root the object from which to start resolution.
     * @param step the path step to resolve.
     * @return a list of {@link Result} objects matching the step.
     */
    public List<Result> resolve1(Object root, String step) {
        Step s = new Step(step);
        return s.resolve(this, root);
    }

    /**
     * Resolves a single path step against a previous result.
     * <p>
     * This may result in multiple results if the step is a wildcard. The path of the new results will be a concatenation
     * of the root's path and the new step.
     *
     * @param root the starting {@link Result}.
     * @param step the path step to resolve.
     * @return a list of {@link Result} objects.
     */
    public List<Result> resolve1(Result root, String step) {
        List<Result> rootResults = resolve1(root.value(), step);
        // concat root results with child results
        return concatResults(root, rootResults);
    }

    /**
     * Recursively navigates through all children of the given roots, collecting all possible results.
     * This is equivalent to a deep traversal of the object graph.
     *
     * @param roots the list of starting {@link Result}s.
     * @return a list containing the initial roots plus all their descendants.
     */
    public List<Result> addAnyChild(List<Result> roots) {
        List<Result> results = new ArrayList<>(roots);

        List<Result> current = roots;
        while (!current.isEmpty()) {
            current = resolveDirectChildren(typeAdapters(), current);
            for(Result result : current) {
                assert !results.contains(result);
            }
            results.addAll(current);
        }
        return results;
    }


    static List<Result> resolveDirectChildren(TypeAdapters typeAdapters, List<Result> roots) {
        List<Result> results = new ArrayList<>();
        for (Result root : roots) {
            List<Result> directChildren = allDirectChildren(typeAdapters, root.value());
            // concat root results with child results
            results.addAll(concatResults(root, directChildren));
        }
        return results;
    }

    private static List<Result> concatResults(Result root, List<Result> directChildren) {
        List<Result> results = new ArrayList<>();
        for(Result directChild : directChildren) {
            List<Object> path = Result.concat(root.path(), directChild.path());
            List<Object> values = Result.concat(root.values(), directChild.values());
            Result result = Result.of(path, values);
            results.add(result);
        }
        return results;
    }

    static List<Result> allDirectChildren(TypeAdapters typeAdapters, Object root) {
        List<Result> results = new ArrayList<>();
        // must convert to map or list
        @Nullable ITypeAdapter<Object, IMapLike> mapAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IMapLike.class);
        if (mapAdapter != null) {
            IMapLike adapted = mapAdapter.toAdapted(root);
            results.addAll(Step.mapAll(adapted));
        }
        @Nullable ITypeAdapter<Object, IListLike> listAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IListLike.class);
        if (listAdapter != null) {
            IListLike adapted = listAdapter.toAdapted(root);
            results.addAll(Step.listAll(adapted));
        }
        return results;
    }

    /**
     * Resolves all descendants of a root object.
     *
     * @param root the object from which to start resolution.
     * @return a list of all {@link Result}s found under the root.
     */
    public List<Result> resolveAny(Object root) {
        List<Result> roots = List.of(Result.ofRoot(root));
        return addAnyChild(roots);
    }

    /**
     * Resolves a single path step against a list of root results and returns a flattened list of new results.
     *
     * @param roots the list of starting {@link Result}s.
     * @param step  the path step to resolve.
     * @return a flattened list of all resolved {@link Result} objects.
     */
    public List<Result> resolve1(List<Result> roots, String step) {
        List<Result> rootResults = new ArrayList<>();
        for (Result root : roots) {
            rootResults.addAll(resolve1(root, step));
        }
        return rootResults;
    }

    /**
     * Resolves a multi-step path against a root object.
     * <p>
     * The path syntax supports property names for map-like objects and bracketed indices for list-like objects.
     * See {@link Step} for more details.
     *
     * @param root at which to resolve the path.
     * @param path a list of strings representing the path steps.
     * @return a list of all matching {@link Result} objects; may be empty.
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
     */
    public List<Result> resolveAll(List<Result> roots, List<String> path) {
        List<Result> current = roots;
        for (String step : path) {
            current = resolve1(current, step);
        }
        return current;
    }

    /**
     * Gets the {@link TypeAdapters} instance used by this resolver.
     *
     * @return the {@link TypeAdapters} instance.
     */
    public TypeAdapters typeAdapters() {
        return adapters;
    }

}
