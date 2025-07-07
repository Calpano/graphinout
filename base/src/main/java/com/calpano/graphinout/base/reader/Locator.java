package com.calpano.graphinout.base.reader;

public interface Locator {

    /** The current location within a stream, e.g., the location after all successfully parsed tokens. */
    Location location();

}
