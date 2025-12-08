package com.graphinout.foundation.pure.input;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class Location {

    public static final Location UNAVAILABLE = new Location(-1, -1);
    private final int line;
    private final int col;

    public Location(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public static Location of(int line, int col) {
        return new Location(line, col);
    }

    public int col() {return col;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Location that = (Location) obj;
        return this.line == that.line &&
                this.col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, col);
    }

    public int line() {return line;}

    @Override
    public @NonNull String toString() {
        if (this == UNAVAILABLE) {
            return "N/A";
        }
        return line + ":" + col;
    }


}
