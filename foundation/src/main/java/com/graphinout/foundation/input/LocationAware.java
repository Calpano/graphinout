package com.graphinout.foundation.input;

import org.jspecify.annotations.Nullable;

/**
 * (Textual) inout can be processed such that the parser knows the {@link Location} within the input file.
 */
public interface LocationAware {

    /** Implementations should override this one */
    @Nullable
    Locator locator();

    /** Implementations should override this one */
    void setLocator(Locator locator);


}
