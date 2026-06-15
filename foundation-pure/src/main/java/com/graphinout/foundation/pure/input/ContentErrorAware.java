package com.graphinout.foundation.pure.input;

import com.graphinout.foundation.pure.functional.Nullables;
import org.jspecify.annotations.Nullable;
import java.util.function.Consumer;

/**
 * Mixin for components that collect and forward {@link ContentError}s through a settable handler.
 */
public interface ContentErrorAware {

    /** Implementations should override this one */
    @Nullable
    Consumer<ContentError> contentErrorHandler();

    /**
     * Convenience method. See {@link IHandleContentErrors} for even smarter helpers.
     *
     * @param contentError to be handled
     */
    default void onContentError(ContentError contentError) {
        @Nullable Consumer<ContentError> nullableConsumer = contentErrorHandler();
        Nullables.ifConsumerPresentAccept(nullableConsumer, contentError);
    }

    /** Implementations should override this one */
    void setContentErrorHandler(Consumer<ContentError> errorHandler);


}
