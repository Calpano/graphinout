package com.graphinout.foundation.pure.functional;

import com.graphinout.foundation.pure.collections.bi.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Ability to send internal data to the given {@link BiConsumer}
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @see Consumer
 */
public interface SourceForBiConsumer<K, L> {

    /**
     * Order is kept
     *
     * @return
     */
    default SourceForBiConsumer<K, L> buffer() {
        List<Pair<K, L>> index = new ArrayList<>();
        forEach((a, b) -> index.add(Pair.create(a, b)));
        return (biConsumer) -> index.forEach(pair -> biConsumer.accept(pair.first(), pair.second()));
    }

    /**
     * @param biConsumer
     */
    void forEach(BiConsumer<K, L> biConsumer);

}
