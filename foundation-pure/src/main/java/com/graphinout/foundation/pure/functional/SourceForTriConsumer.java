package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * Ability to send internal data to the given {@link TriConsumer}
 * <p>
 * To instantiate, just use <code>instance::forEachToConsumer</code> on your instance, which has a method to send data
 * to the given {@link TriConsumer}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface SourceForTriConsumer<K, L, M> {

    /**
     * @param triConsumer to receive triples
     */
    void forEach(TriConsumer<K, L, M> triConsumer);

}
