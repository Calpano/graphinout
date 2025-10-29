package com.graphinout.foundation.input;

import javax.annotation.Nullable;

public interface Locator {

    static Location locationOrNotAvailable(@Nullable Locator locator) {
        return locator == null ? Location.UNAVAILABLE : locator.location();
    }

    /** The current location within a stream, e.g., the location after all successfully parsed tokens. */
    Location location();

}
