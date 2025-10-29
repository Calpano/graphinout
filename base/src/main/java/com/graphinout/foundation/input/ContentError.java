package com.graphinout.foundation.input;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * See {@link ContentErrors} for simpler handlers.
 */
public class ContentError {

    public enum ErrorLevel {
        Warn, Error
    }

    final ErrorLevel level;
    final String message;
    final @Nullable Location location;

    public ContentError(ErrorLevel level, String message, @Nullable Location location) {
        this.level = level;
        this.message = message;
        this.location = location;
    }

    public static ContentError of(ErrorLevel level, String message, @Nullable Location location) {
        return new ContentError(level, message, location);
    }

    public static ContentError of(ErrorLevel level, String message) {
        return new ContentError(level, message, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentError that)) return false;
        return level == that.level && message.equals(that.message) && Objects.equals(location, that.location);
    }

    public ErrorLevel getLevel() {
        return level;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message, location);
    }

    public Optional<Location> location() {
        return Optional.ofNullable(location);
    }

    @Override
    public String toString() {
        return "ContentError{" + "level=" + level + ", message='" + message + '\'' + ", location=" + location + '}';
    }

}
