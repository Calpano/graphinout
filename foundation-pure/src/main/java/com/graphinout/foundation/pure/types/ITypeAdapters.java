package com.graphinout.foundation.pure.types;

import org.jspecify.annotations.Nullable;

public interface ITypeAdapters {

    <I, O> @Nullable O adaptTo(I inputValue, Class<O> targetType);

    /**
     * NOTE: Due to a generics problem we use {@code Class<?> inputClass} instead of {@code Class<I>}
     * @param inputClass
     * @param outputClass
     * @param warnIfNoneFound
     * @return
     * @param <I>
     * @param <O>
     */
    <I, O> @Nullable ITypeAdapter<I, O> findAdapterFromTo(Class<?> inputClass, Class<O> outputClass, boolean warnIfNoneFound);

    /**
     *
     * @param inputType  for which to register an adapter
     * @param targetType which the adapter can produce
     * @param adapter    given a T it can adapt it to a R
     * @param <I>        input type
     * @param <O>        output type
     */
    <I, O> void register(Class<I> inputType, Class<O> targetType, ITypeAdapter<I, O> adapter);


}
