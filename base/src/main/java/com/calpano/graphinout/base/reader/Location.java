package com.calpano.graphinout.base.reader;

import java.util.Objects;

public class Location {

    int line;
    int col;

    public Location(int lineNumber, int columnNumber) {
        this.line = lineNumber;
        this.col = columnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return line == location.line && col == location.col;
    }

    public int getCol() {
        return col;
    }

    public int getLine() {
        return line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, col);
    }

    @Override
    public String toString() {
        return line + ":" + col;
    }

}
