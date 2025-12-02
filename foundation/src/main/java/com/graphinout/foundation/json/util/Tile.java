package com.graphinout.foundation.json.util;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.graphinout.foundation.json.util.JsonCompactFormatter.NEWLINE;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE;

public class Tile {

    private final List<String> lines = new ArrayList<>();
    /** fixed layout? */
    private boolean isFixed = false;

    public Tile(boolean isFixed) {
        this.isFixed = isFixed;
    }

    public static Tile create(boolean isFixed) {
        return new Tile(isFixed);
    }

    public static Tile of(String line) {
        Tile tile = new Tile(false);
        tile.addLine(line);
        return tile;
    }

    public static Tile of(List<String> line) {
        Tile tile = new Tile(false);
        tile.lines.addAll(line);
        return tile;
    }

    public void add(Tile childTile) {
        lines.addAll(childTile.lines);
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public int height() {
        return lines.size();
    }

    public void insertFirstLineLeft(String insert) {
        lines.set(0, insert + lines.getFirst());
    }

    public void insertLastLineRight(String insert) {
        lines.set(lines.size() - 1, lines.getLast() + insert);
    }

    /**
     * Must contain at least 1 line.
     *
     * @param first prepended to first line
     * @param body  prepended to each line in the middle
     */
    public void insertLeft(String first, String body) {
        assert first.length() == body.length();
        assert height() >= 1;
        lines.set(0, first + lines.getFirst());
        for (int i = 1; i < lines.size(); i++) {
            lines.set(i, body + lines.get(i));
        }
    }

    /** Prepend each line with it */
    public void insertLeft(String insert) {
        lines.replaceAll(s -> insert + s);
    }

    public void insertLineAbove(String line) {
        lines.addFirst(line);
    }

    public void insertRight(String insert) {
        lines.replaceAll(s -> s + insert);
    }

    public boolean isFixed() {
        return isFixed;
    }

    public List<String> lines() {
        return lines;
    }

    /**
     * Attempt to render a tile as a single line.
     *
     * @param maxLineLength
     * @return single line or null
     */
    public @Nullable String toSingleLine(int maxLineLength) {
        StringBuilder target = new StringBuilder();
        for (int i = 0; i < lines().size(); i++) {
            if (i > 0) {
                target.append(SPACE);
            }
            // the trim is important
            String line = lines.get(i).trim();
            target.append(line);
            if (target.length() > maxLineLength) {
                return null;
            }
        }
        return target.toString();
    }

    @Override
    public String toString() {
        return String.join(NEWLINE, lines);
    }

    public int width() {
        return lines.stream().mapToInt(String::length).max().orElse(0);
    }

}
