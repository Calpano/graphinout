package com.calpano.graphinout.foundation.util.path;

import com.calpano.graphinout.foundation.util.MapMap;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/** Can I extend arbitrary classes with additional methods by registering matching interfaces? */
public class TypeAdapters {

    private static final Logger log = getLogger(TypeAdapters.class);
    /** sourceType - targetType  - adapter */
    private final MapMap<Class<?>, Class<?>, ITypeAdapter<?, ?>> adapters = new MapMap<>();

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

    @SuppressWarnings("unchecked")
    public <T, A> A adaptOrThrow(T value, Class<A> targetType) {
        Class<?> sourceType = value.getClass();
        ITypeAdapter<T, A> adapter = findAdapterFromTo(sourceType, targetType, null);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter registered for " + value.getClass() + " (or any of its super-types) -> " + targetType);
        }
        return adapter.toAdapted(value);
    }

    public <T, A> @Nullable A adaptTo(T value, Class<A> targetType) {
        Class<?> sourceType = value.getClass();
        ITypeAdapter<T, A> adapter = findAdapterFromTo(sourceType, targetType, null);
        if (adapter == null) {
            return null;
        }
        return adapter.toAdapted(value);
    }

    /** Simplistic exact lookup */
    public <T, A> @Nullable ITypeAdapter<T, A> exactAdapterFromTo(Class<?> sourceType, Class<?> targetType) {
        return (ITypeAdapter<T, A>) adapters.get(sourceType, targetType);
    }

    public <A, T> @Nullable ITypeAdapter<T, A> findAdapterFromTo(Class<?> sourceType, Class<A> targetType) {
        return findAdapterFromTo(sourceType, targetType, null);
    }

    public <A, T> @Nullable ITypeAdapter<T, A> findAdapterFromTo(Class<?> sourceType, Class<A> targetType, @Nullable HashSet<Class<?>> alreadyTried) {
        ITypeAdapter<T, A> adapter = exactAdapterFromTo(sourceType, targetType);
        if (adapter != null)
            return adapter;

        // hard mode
        log.trace("No direct adapter from {} to {}", sourceType.getSimpleName(), targetType.getSimpleName());
        HashSet<Class<?>> tried = alreadyTried == null ? new HashSet<>() : alreadyTried;
        tried.add(sourceType);

        //try interfaces of sourceType
        for (Class<?> interfaceType : allInterfacesOf(sourceType)) {
            adapter = exactAdapterFromTo(interfaceType, targetType);
            if (adapter != null) {
                log.trace("FOUND interface adapter from {} ({}) to {}", interfaceType.getSimpleName(), sourceType.getSimpleName(), targetType.getSimpleName());
                return adapter;
            }
            tried.add(interfaceType);
            log.trace("No interface adapter from {} ({}) to {}", interfaceType.getSimpleName(), sourceType.getSimpleName(), targetType.getSimpleName());
        }
        // try super-types
        Class<?> superType = sourceType.getSuperclass();
        if (superType == null) {
            // found no adapter yet and have no more super-types to try
            log.warn("No adapter found to {}. Tried types: {}", targetType.getSimpleName(), tried);
            return null;
        }
        return findAdapterFromTo(superType, targetType, tried);
    }

    public <T, A> void register(Class<T> sourceType, Class<A> targetType, ITypeAdapter<T, A> adapter) {
        adapters.put(sourceType, targetType, adapter);
    }

}
