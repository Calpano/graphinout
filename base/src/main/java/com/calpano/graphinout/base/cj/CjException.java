package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.json.JsonException;

public class CjException extends JsonException {

    public CjException(final String message) {
        super(message);
    }

    public CjException(final Throwable t) {
        super(t);
    }

    public CjException(final String message, final Throwable t) {
        super(message, t);
    }

}
