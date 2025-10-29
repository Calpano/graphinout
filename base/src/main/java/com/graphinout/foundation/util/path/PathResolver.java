package com.graphinout.foundation.util.path;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
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

    private final TypeAdapters adapters = new TypeAdapters();

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
        registerMap(ICjData.class, data -> {
            IJsonValue jsonValue = data.jsonValue();
            if (jsonValue == null) return IMapLike.EMPTY;
            return jsonValue.asMapLike();
        });

    }

    /**
     * Factory method to create a new {@link PathResolver} instance.
     *
     * @return a new {@link PathResolver}.
     */
    public static PathResolver create() {
        return new PathResolver();
    }

    static List<Object> getDirectChildrenOf1(TypeAdapters typeAdapters, Object root) {
        assert root != null;
        List<Object> results = new ArrayList<>();
        // must convert to map or list
        @Nullable ITypeAdapter<Object, IMapLike> mapAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IMapLike.class, false);
        if (mapAdapter != null) {
            IMapLike adapted = mapAdapter.toAdapted(root);
            adapted.keys().forEach(key -> {
                Object child = adapted.get(key);
                if (child != null)
                    results.add(child);
            });
        }
        @Nullable ITypeAdapter<Object, IListLike> listAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IListLike.class, false);
        if (listAdapter != null) {
            IListLike adapted = listAdapter.toAdapted(root);
            for (int i = 0; i < adapted.size(); i++) {
                Object child = adapted.get(i);
                if (child != null)
                    results.add(child);
            }
        }
        return results;
    }

    static List<Object> getDirectChildrenOfList(TypeAdapters typeAdapters, List<Object> roots) {
        List<Object> results = new ArrayList<>();
        for (Object root : roots) {
            assert root != null;
            List<Object> directChildren = getDirectChildrenOf1(typeAdapters, root);
            results.addAll(directChildren);
        }
        return results;
    }

    static List<Result> resolveDirectChildren(TypeAdapters typeAdapters, Object root) {
        List<Result> results = new ArrayList<>();
        // must convert to map or list
        @Nullable ITypeAdapter<Object, IMapLike> mapAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IMapLike.class, false);
        if (mapAdapter != null) {
            IMapLike adapted = mapAdapter.toAdapted(root);
            results.addAll(Step.mapAll(adapted));
        }
        @Nullable ITypeAdapter<Object, IListLike> listAdapter = typeAdapters.findAdapterFromTo(root.getClass(), IListLike.class, false);
        if (listAdapter != null) {
            IListLike adapted = listAdapter.toAdapted(root);
            results.addAll(Step.listAll(adapted));
        }
        return results;
    }

    /**
     * Recursively navigates through all children of the given roots, collecting all possible results. This is
     * equivalent to a deep traversal of the object graph.
     *
     * @param root for starting {@link Result}s.
     * @return a list containing the initial roots plus all their descendants.
     */
    public List<Result> addAnyChild(Object root) {
        // each any-child is ONE STEP (..) derived from roots.
        List<Result> anyChildFull = new ArrayList<>();

        assert root != null;
        // each root itself is a .. child of itself
        Result rootChildFull = Result.ofStep("..", root);
        anyChildFull.add(rootChildFull);

        // breadth-first search through the tree of maps/lists
        List<Object> current = List.of(root);
        while (!current.isEmpty()) {
            current = getDirectChildrenOfList(typeAdapters(), current);
            for(Object child : current) {
                Result childFull = Result.ofStep("..", child);
                anyChildFull.add(childFull);
            }
        }

        return anyChildFull;
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
     * This may result in multiple results if the step is a wildcard. The path of the new results will be a
     * concatenation of the root's path and the new step.
     *
     * @param root the starting {@link Result}.
     * @param step the path step to resolve.
     * @return a list of {@link Result} objects.
     */
    public List<Result> resolve1(Result root, String step) {
        List<Result> rootResults = resolve1(root.value(), step);
        // concat root results with child results
        return Result.concatResults(root, rootResults);
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
     * The path syntax supports property names for map-like objects and bracketed indices for list-like objects. See
     * {@link Step} for more details.
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
     * Resolves all descendants of a root object.
     *
     * @param root the object from which to start resolution.
     * @return a list of all {@link Result}s found under the root.
     */
    public List<Result> resolveAny(Object root) {
        return addAnyChild(root);
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
