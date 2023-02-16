package com.calpano.graphinout.base.reader;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public
class ContentError {
    public enum ErrorLevel {
        Warn, Error
    }

    public class Location {
        int line;
        int col;
    }

    final ErrorLevel level;
    final String message;
    final Optional<Location> location;
}
