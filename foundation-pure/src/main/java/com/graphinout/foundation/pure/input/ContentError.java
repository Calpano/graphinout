package com.graphinout.foundation.pure.input;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * See {@code ContentErrors} for simpler handlers.
 */
public class ContentError {

    public enum ErrorLevel {
        Error, Warn, Info
    }

    public final ErrorLevel level;
    public final String message;
    public final @Nullable Location location;

    public ContentError(ErrorLevel level, String message, @Nullable Location location) {
        this.level = level;
        this.message = message;
        this.location = location;
    }

    public static ContentError error(String message) {
        return new ContentError(ErrorLevel.Error, message, Location.UNAVAILABLE);
    }

    public static ContentError of(ErrorLevel level, String message) {
        return new ContentError(level, message, null);
    }

    public static ContentError of(ErrorLevel level, String message, @Nullable Location location) {
        return new ContentError(level, message, location);
    }

    public static void try_(Runnable runnable, String messageDetail, Consumer<ContentError> errorHandler) {
        try {
            runnable.run();
        } catch (Throwable t) {
            ContentError.warn(messageDetail + " Reason: " + t.getMessage()).fireTo(errorHandler);
        }
    }

    public static ContentError warn(String message) {
        return new ContentError(ErrorLevel.Warn, message, Location.UNAVAILABLE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentError)) return false;
        ContentError that = (ContentError) o;
        return level == that.level && message.equals(that.message) && Objects.equals(location, that.location);
    }

    public void fireTo(Consumer<ContentError> errorHandler) {
        errorHandler.accept(this);
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

    public boolean isError() {
        return level == ErrorLevel.Error;
    }

    public boolean isInfo() {
        return level == ErrorLevel.Info;
    }

    public boolean isWarn() {
        return level == ErrorLevel.Warn;
    }

    public Optional<Location> location() {
        return Optional.ofNullable(location);
    }

    @Override
    public String toString() {
        return "ContentError{" + "level=" + level + ", message='" + message + '\'' + ", location=" + location + '}';
    }

}
