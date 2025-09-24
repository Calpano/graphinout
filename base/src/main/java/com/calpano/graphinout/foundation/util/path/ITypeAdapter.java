package com.calpano.graphinout.foundation.util.path;

/**
 * A functional interface for converting an object of one type to another.
 * <p>
 * This is used, e.g., by the {@link PathResolver} to convert arbitrary objects into {@link IMapLike} or {@link IListLike}
 * instances, allowing them to be navigated.
 *
 * @param <T> the type of the input value.
 * @param <A> the type of the adapted value.
 */
@FunctionalInterface
public interface ITypeAdapter<T, A> {

    /**
     * Converts the given value to the adapted type.
     *
     * @param value the object to convert.
     * @return the adapted object.
     */
    A toAdapted(T value);

}
