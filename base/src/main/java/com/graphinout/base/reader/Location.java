package com.graphinout.base.reader;

import edu.umd.cs.findbugs.annotations.NonNull;

public record Location(int line, int col) {

    public static final Location UNAVAILABLE = new Location(-1, -1);

    public static Location of(int line, int col) {
        return new Location(line, col);
    }

    @Override
    public @NonNull String toString() {
        if (this == UNAVAILABLE) {
            return "N/A";
        }
        return line + ":" + col;
    }

}
