package com.calpano.graphinout.foundation.json;

public class JsonException extends RuntimeException {

    public JsonException(final String message) {
        super(message);
    }

    public JsonException(final Throwable t) {
        super(t);
    }

    public JsonException(final String message, final Throwable t) {
        super(message, t);
    }

}
