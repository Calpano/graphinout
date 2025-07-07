package com.calpano.graphinout.base.reader;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

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

    public ErrorLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentError that)) return false;
        return level == that.level && message.equals(that.message) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message, location);
    }

    public Optional<Location> location() {
        return Optional.of(location);
    }

    @Override
    public String toString() {
        return "ContentError{" + "level=" + level + ", message='" + message + '\'' + ", location=" + location + '}';
    }
}
