package com.graphinout.foundation.pure.input;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface Locator {

    static Location locationOrNotAvailable(@Nullable Locator locator) {
        return locator == null ? Location.UNAVAILABLE : locator.location();
    }

    /** The current location within a stream, e.g., the location after all successfully parsed tokens. */
    Location location();

}
