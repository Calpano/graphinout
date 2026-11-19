package com.graphinout.base.fetch;

import java.io.IOException;

/** Remote content was refused (policy, URL, budget) or could not be loaded. The message says which and why. */
public class RemoteContentException extends IOException {

    public RemoteContentException(String message) {
        super(message);
    }

    public RemoteContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
