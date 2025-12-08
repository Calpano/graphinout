package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * Ability to send internal data to the given {@link QuadConsumerFilter}
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @param <O> the type of the fourth argument to the operation
 * @param <P> the type of the fifth argument to the operation
 * @see Consumer
 */
public interface SourceForQuinConsumerFilter<K, L, M, O, P> {

    /**
     * @param quinConsumer (k,l,m,o,p)
     */
    void forEach(QuinConsumerFilter<K, L, M, O, P> quinConsumer);

}
