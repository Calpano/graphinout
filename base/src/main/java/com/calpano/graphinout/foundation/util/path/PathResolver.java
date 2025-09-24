package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class PathResolver {

    private final TypeAdapters adapters = new TypeAdapters();

    public PathResolver() {
        // add trivial adapters
        registerList(List.class, IListLike::ofList);
        registerMap(Map.class, IMapLike::ofMap);
        registerMap(IJsonObject.class, jo -> IMapLike.of(() -> jo.keys().stream().sorted().toList(), jo::get));
        registerList(IJsonArray.class, ja -> IListLike.of(ja::size, ja::get));
    }

    public static PathResolver create() {
        return new PathResolver();
    }

    public <T> PathResolver registerList(Class<T> type, ITypeAdapter<T, IListLike> adapter) {
        adapters.register(type, IListLike.class, adapter);
        return this;
    }

    public <T> PathResolver registerMap(Class<T> type, ITypeAdapter<T, IMapLike> adapter) {
        adapters.register(type, IMapLike.class, adapter);
        return this;
    }

    /** resolve one step in a single object, which may result in multiple results (list-all) */
    public List<Result> resolve1(Object root, String step) {
        Step s = new Step(step);
        return s.resolve(this, root);
    }

    /** resolve one step in a single root, which may result in multiple results (list-all) */
    public List<Result> resolve1(Result root, String step) {
        List<Result> rootResults = resolve1(root.value(), step);
        // concat root results with child results
        return concatResults(root, rootResults);
    }

    /** Navigate any number of steps down */
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







    public List<Result> resolveAny(Object root) {
        List<Result> roots = List.of(Result.ofRoot(root));
        return addAnyChild(roots);
    }

    /** resolve one step in a list of roots as a flat list */
    public List<Result> resolve1(List<Result> roots, String step) {
        List<Result> rootResults = new ArrayList<>();
        for (Result root : roots) {
            rootResults.addAll(resolve1(root, step));
        }
        return rootResults;
    }

    /**
     * NAME := [a-zA-Z][a-zA-Z0-9]* (like a CSS/XML/HTML ID)
     *
     * @param root at which to resolve the step.
     * @param path see {@link Step} for details.
     * @return all matching objects; may be empty.
     */
    public List<Result> resolveAll(Object root, List<String> path) {
        List<Result> roots = List.of(Result.ofRoot(root));
        return resolveAll(roots, path);
    }

    public List<Result> resolveAll(List<Result> roots, List<String> path) {
        List<Result> current = roots;
        for (String step : path) {
            current = resolve1(current, step);
        }
        return current;
    }

    public TypeAdapters typeAdapters() {
        return adapters;
    }

}
