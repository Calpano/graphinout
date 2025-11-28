package com.graphinout.foundation.input;

import org.jspecify.annotations.Nullable;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifConsumerPresentAccept;

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
        ifConsumerPresentAccept(contentErrorHandler(), contentError);
    }

    /** Implementations should override this one */
    void setContentErrorHandler(Consumer<ContentError> errorHandler);


}
