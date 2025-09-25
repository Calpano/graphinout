package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.util.MapMap;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages a registry of {@link ITypeAdapter}s and provides methods to find and apply them.
 * <p>
 * This class allows for extending arbitrary classes with additional behaviors by registering adapters that can convert
 * them to desired interface types like {@link IMapLike} or {@link IListLike}. It supports searching for adapters for
 * super-classes and interfaces if a direct adapter is not found.
 */
public class TypeAdapters {

    private static final Logger log = getLogger(TypeAdapters.class);
    /** sourceType - targetType  - adapter */
    private final MapMap<Class<?>, Class<?>, ITypeAdapter<?, ?>> adapters = new MapMap<>();

    /**
     * Recursively finds all interfaces implemented by a given class and its super-classes/interfaces.
     *
     * @param type the class to inspect.
     * @return a {@link Set} of all implemented interfaces.
     */
    public static Set<Class<?>> allInterfacesOf(Class<?> type) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> interfaceType : type.getInterfaces()) {
            result.add(interfaceType);
            // RECURSE
            result.addAll(allInterfacesOf(interfaceType));

            Class<?> superType = interfaceType.getSuperclass();
            if (superType != null) {
                result.add(superType);
                // RECURSE
                result.addAll(allInterfacesOf(superType));
            }
        }
        return result;
    }

    /**
     * Internal implementation for finding an adapter, with a mechanism to avoid cycles during recursive search.
     *
     * @param <A>                the target type.
     * @param <T>                the source type.
     * @param originalSourceType the source class for logging
     * @param refinedSourceType  the refined source class to look up
     * @param targetType         the target class.
     * @param alreadyTried       a set of classes that have already been checked, to prevent infinite loops.
     * @param warnIfNoneFound
     * @return the matching {@link ITypeAdapter}, or {@code null} if not found.
     */
    public <A, T> @Nullable ITypeAdapter<T, A> _findAdapterFromTo(Class<?> originalSourceType, Class<?> refinedSourceType, Class<A> targetType, @Nullable HashSet<Class<?>> alreadyTried, boolean warnIfNoneFound) {
        ITypeAdapter<T, A> adapter = exactAdapterFromTo(refinedSourceType, targetType);
        if (adapter != null) return adapter;

        // hard mode
        log.trace("No direct adapter from {} to {}", refinedSourceType.getSimpleName(), targetType.getSimpleName());
        HashSet<Class<?>> tried = alreadyTried == null ? new HashSet<>() : alreadyTried;
        tried.add(refinedSourceType);

        //try interfaces of sourceType
        for (Class<?> interfaceType : allInterfacesOf(refinedSourceType)) {
            adapter = exactAdapterFromTo(interfaceType, targetType);
            if (adapter != null) {
                log.trace("FOUND interface adapter from {} ({}) to {}", interfaceType.getSimpleName(), originalSourceType.getSimpleName(), targetType.getSimpleName());
                return adapter;
            }
            tried.add(interfaceType);
            log.trace("No interface adapter from {} ({}) to {}", interfaceType.getSimpleName(), originalSourceType.getSimpleName(), targetType.getSimpleName());
        }
        // try super-types
        Class<?> superType = refinedSourceType.getSuperclass();
        if (superType == null) {
            // found no adapter yet and have no more super-types to try
            if (warnIfNoneFound) {
                log.warn("No adapter found from={} to target={}. Tried types: {}", originalSourceType.getSimpleName(), targetType.getSimpleName(), tried);
            }
            return null;
        }
        return _findAdapterFromTo(originalSourceType, superType, targetType, tried, warnIfNoneFound);
    }

    /**
     * Adapts a value to the specified target type, throwing an exception if no adapter is found.
     *
     * @param value      the object to adapt.
     * @param targetType the class of the target type.
     * @param <T>        the source type.
     * @param <A>        the target type.
     * @return the adapted object.
     * @throws IllegalArgumentException if no suitable adapter can be found.
     */
    public <T, A> A adaptOrThrow(T value, Class<A> targetType) {
        Class<?> sourceType = value.getClass();
        ITypeAdapter<T, A> adapter = _findAdapterFromTo(sourceType, sourceType, targetType, null, true);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter registered for " + value.getClass() + " (or any of its super-types) -> " + targetType);
        }
        return adapter.toAdapted(value);
    }

    /**
     * Adapts a value to the specified target type.
     *
     * @param value      the object to adapt.
     * @param targetType the class of the target type.
     * @param <T>        the source type.
     * @param <A>        the target type.
     * @return the adapted object, or {@code null} if no suitable adapter can be found.
     */
    public <T, A> @Nullable A adaptTo(T value, Class<A> targetType) {
        Class<?> sourceType = value.getClass();
        ITypeAdapter<T, A> adapter = _findAdapterFromTo(sourceType, sourceType, targetType, null, false);
        if (adapter == null) {
            return null;
        }
        return adapter.toAdapted(value);
    }

    /**
     * Finds an adapter that exactly matches the given source and target types, without searching super-types or
     * interfaces.
     *
     * @param sourceType the source class.
     * @param targetType the target class.
     * @param <T>        the source type.
     * @param <A>        the target type.
     * @return the matching {@link ITypeAdapter}, or {@code null} if not found.
     */
    public <T, A> @Nullable ITypeAdapter<T, A> exactAdapterFromTo(Class<?> sourceType, Class<?> targetType) {
        return (ITypeAdapter<T, A>) adapters.get(sourceType, targetType);
    }

    /**
     * Finds an adapter for the given source and target types, searching super-classes and interfaces if no direct
     * adapter is found.
     *
     * @param <A>             the target type.
     * @param <T>             the source type.
     * @param sourceType      the source class.
     * @param targetType      the target class.
     * @param warnIfNoneFound
     * @return the matching {@link ITypeAdapter}, or {@code null} if not found.
     */
    public <A, T> @Nullable ITypeAdapter<T, A> findAdapterFromTo(Class<?> sourceType, Class<A> targetType, boolean warnIfNoneFound) {
        return _findAdapterFromTo(sourceType, sourceType, targetType, null, warnIfNoneFound);
    }

    /**
     * Registers a new type adapter.
     *
     * @param sourceType the class of the source type.
     * @param targetType the class of the target type.
     * @param adapter    the adapter to register.
     * @param <T>        the source type.
     * @param <A>        the target type.
     */
    public <T, A> void register(Class<T> sourceType, Class<A> targetType, ITypeAdapter<T, A> adapter) {
        adapters.put(sourceType, targetType, adapter);
    }

}
