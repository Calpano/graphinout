package com.graphinout.foundation.input;

import javax.annotation.Nullable;

public class ContentErrorException extends RuntimeException {

    private final ContentError.ErrorLevel errorLevel;
    private final Location location;

    public ContentErrorException(ContentError contentError, Throwable cause) {
        this(contentError.getLevel(), contentError.getMessage(), cause, contentError.getLocation());
    }

    public ContentErrorException(ContentError contentError) {
        this(contentError.getLevel(), contentError.getMessage(), contentError.getLocation());
    }

    public ContentErrorException(ContentError.ErrorLevel errorLevel, String message, Throwable cause, Location location) {
        super(message, cause);
        this.errorLevel = errorLevel;
        this.location = location;
    }

    public ContentErrorException(ContentError.ErrorLevel errorLevel, String message, Location location) {
        super(message);
        this.errorLevel = errorLevel;
        this.location = location;
    }

    public static ContentErrorException of(ContentError contentError, @Nullable Throwable cause) {
        return cause == null ? new ContentErrorException(contentError) : new ContentErrorException(contentError, cause);
    }

    public ContentError.ErrorLevel errorLevel() {
        return errorLevel;
    }

    public Location location() {
        return location;
    }

}
