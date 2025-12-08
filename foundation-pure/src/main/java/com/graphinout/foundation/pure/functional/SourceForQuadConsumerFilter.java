package com.graphinout.foundation.pure.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Ability to send internal data to the given {@link QuadConsumerFilter}
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @param <O> the type of the fourth argument to the operation
 * @see Consumer
 */
public interface SourceForQuadConsumerFilter<K, L, M, O> {

    default SourceForBiConsumer<K, O> adaptToBiConsumerFirstLast(final Predicate<L> predicateL,
                                                                 final Predicate<M> predicateM) {
        return biConsumer -> forEachFirstLast(predicateL, predicateM, biConsumer);
    }

    default SourceForBiConsumer<K, O> adaptoToBiConsumer_filter124_send14(final TriPredicate<K, L, O> predicateKLO) {
        return biConsumer -> forEach_filter124_send14(predicateKLO, biConsumer);
    }

    /**
     * @param quadConsumer (k,l,m,o)
     */
    void forEach(QuadConsumerFilter<K, L, M, O> quadConsumer);

    /**
     * Adapt quads to bi. Filter them.
     *
     * @param predicateL @Nullable for wild-card
     * @param predicateM @Nullable for wild-card
     * @param biConsumer
     */
    default void forEachFirstLast(final Predicate<L> predicateL, final Predicate<M> predicateM,
                                  final BiConsumer<K, O> biConsumer) {
        forEach((k, l, m, o) -> {
            if (predicateL != null && !predicateL.test(l)) {
                return true;
            }
            if (predicateM != null && !predicateM.test(m)) {
                return true;
            }
            biConsumer.accept(k, o);
            return true;
        });
    }

    /**
     * Filter on quad components (1,2, and 4), send projected quad components (1,4)
     *
     * @param predicateKLO
     * @param biConsumer
     */
    default void forEach_filter124_send14(final TriPredicate<K, L, O> predicateKLO, final BiConsumer<K, O> biConsumer) {
        forEach((k, l, m, o) -> {
            if (predicateKLO != null && !predicateKLO.test(k, l, o)) {
                return true;
            }
            biConsumer.accept(k, o);
            return true;
        });
    }

}
