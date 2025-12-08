package com.graphinout.foundation.pure.functional;


import com.graphinout.foundation.pure.value.LongRef;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * IO-version of {@link ISource}. Has {@link IOException}, while {@link ISource} does not.
 * <p>
 * Streams a list of E to a consumer. Repeatable.
 * <p>
 * A source can be streamed to a {@link Consumer} of E's.
 * <p>
 * Similar to an {@link Iterable}, where one can get an {@link Iterator} of elements -- this class can be called to push
 * elements to a consumer, repeatedly.
 *
 * @author xamde
 */
public interface IOSource<E> {

    /**
     * @return number of elements delivered by source
     * @throws IOException
     */
    default long count() throws IOException {
        final LongRef count = new LongRef();
        streamTo(x -> count.value++);
        return count.value;
    }

    /**
     * @param sourceHandler
     * @throws IOException
     */
    void streamTo(Consumer<E> sourceHandler) throws IOException;

}
