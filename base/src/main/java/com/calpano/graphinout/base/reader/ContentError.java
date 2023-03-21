package com.calpano.graphinout.base.reader;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class ContentError {
    public enum ErrorLevel {
        Warn, Error
    }

    public static class Location {
        int line;
        int col;

        public Location(int lineNumber, int columnNumber) {
            this.line = lineNumber;
            this.col = columnNumber;
        }

        @Override
        public String toString() {
            return line + ":" + col;
        }
    }

    final ErrorLevel level;
    final String message;
    final @Nullable Location location;

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
