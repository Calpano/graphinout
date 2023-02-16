package com.calpano.graphinout.base.reader;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public
class ContentError {
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
    }

    final ErrorLevel level;
    final String message;
    final Optional<Location> location;
}
