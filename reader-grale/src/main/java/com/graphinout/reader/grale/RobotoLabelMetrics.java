package com.graphinout.reader.grale;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimates the on-screen size of a node box from its label text, for writers that must supply
 * dagre/grale {@code width}/{@code height} when the source format carries none.
 *
 * <p>The estimate assumes the <b>Roboto</b> Regular font at {@link #DEFAULT_FONT_SIZE 16&nbsp;px}. Rather
 * than load a real font (which would make the result platform-dependent and break grale's determinism
 * guarantee), it uses a fixed table of per-character advance widths in em units derived from Roboto
 * Regular. Widths are therefore approximate but deterministic.
 *
 * <h2>Wrapping</h2>
 * The label is split into lines first on explicit {@code <br>} tags ({@code <br>}, {@code <br/>},
 * {@code <br />}; case-insensitive), then greedily word-wrapped so no line exceeds
 * {@link #DEFAULT_MAX_LINE_LENGTH 50} characters. A single word longer than the limit is kept on its
 * own line rather than hard-split.
 *
 * <p>The box adds {@link #PADDING_X}/{@link #PADDING_Y} of padding on each side around the text.
 */
public final class RobotoLabelMetrics {

    /** Font assumed for the estimate (informational; the advance table is Roboto Regular). */
    public static final String DEFAULT_FONT = "Roboto";
    /** Font size assumed for the estimate, in px. */
    public static final double DEFAULT_FONT_SIZE = 16.0;
    /** Maximum characters per line before word-wrapping. */
    public static final int DEFAULT_MAX_LINE_LENGTH = 50;
    /** Line height as a multiple of the font size. */
    public static final double LINE_HEIGHT_FACTOR = 1.2;
    /** Horizontal padding added on each side of the text, in px. */
    public static final double PADDING_X = 8.0;
    /** Vertical padding added on each side of the text, in px. */
    public static final double PADDING_Y = 4.0;

    /** Per-character advance widths for Roboto Regular, in em (fraction of the font size). */
    private static final double[] ADVANCE = new double[128];
    private static final double DEFAULT_ADVANCE = 0.55;

    static {
        for (int i = 0; i < ADVANCE.length; i++) ADVANCE[i] = DEFAULT_ADVANCE;
        put(' ', 0.25); put('!', 0.26); put('"', 0.33); put('#', 0.55); put('$', 0.55);
        put('%', 0.78); put('&', 0.62); put('\'', 0.18); put('(', 0.33); put(')', 0.33);
        put('*', 0.39); put('+', 0.55); put(',', 0.27); put('-', 0.33); put('.', 0.27);
        put('/', 0.42); put(':', 0.27); put(';', 0.27); put('<', 0.55); put('=', 0.55);
        put('>', 0.55); put('?', 0.46); put('@', 0.90);
        put('A', 0.66); put('B', 0.63); put('C', 0.66); put('D', 0.67); put('E', 0.60);
        put('F', 0.58); put('G', 0.69); put('H', 0.71); put('I', 0.29); put('J', 0.55);
        put('K', 0.64); put('L', 0.55); put('M', 0.86); put('N', 0.71); put('O', 0.71);
        put('P', 0.62); put('Q', 0.71); put('R', 0.64); put('S', 0.62); put('T', 0.60);
        put('U', 0.68); put('V', 0.65); put('W', 0.91); put('X', 0.64); put('Y', 0.62);
        put('Z', 0.62); put('[', 0.30); put('\\', 0.42); put(']', 0.30); put('^', 0.45);
        put('_', 0.45); put('`', 0.33);
        put('a', 0.54); put('b', 0.56); put('c', 0.52); put('d', 0.56); put('e', 0.54);
        put('f', 0.35); put('g', 0.56); put('h', 0.56); put('i', 0.24); put('j', 0.24);
        put('k', 0.51); put('l', 0.24); put('m', 0.86); put('n', 0.56); put('o', 0.56);
        put('p', 0.56); put('q', 0.56); put('r', 0.36); put('s', 0.51); put('t', 0.34);
        put('u', 0.56); put('v', 0.50); put('w', 0.74); put('x', 0.50); put('y', 0.50);
        put('z', 0.50); put('{', 0.34); put('|', 0.25); put('}', 0.34); put('~', 0.55);
        put('0', 0.55); put('1', 0.55); put('2', 0.55); put('3', 0.55); put('4', 0.55);
        put('5', 0.55); put('6', 0.55); put('7', 0.55); put('8', 0.55); put('9', 0.55);
    }

    private static void put(char c, double emWidth) {
        ADVANCE[c] = emWidth;
    }

    private RobotoLabelMetrics() {}

    /** An estimated node box size, in px, rounded to whole pixels. */
    public record Box(int width, int height) {}

    /** Estimate the node box for {@code label} using the defaults (Roboto, 16&nbsp;px, max 50 chars). */
    public static Box estimate(@Nullable String label) {
        return estimate(label, DEFAULT_FONT_SIZE, DEFAULT_MAX_LINE_LENGTH);
    }

    /** Estimate the node box for {@code label} at the given font size and wrap width. */
    public static Box estimate(@Nullable String label, double fontSize, int maxLineLength) {
        List<String> lines = wrap(label, maxLineLength);
        double maxLineWidthEm = 0;
        for (String line : lines) {
            maxLineWidthEm = Math.max(maxLineWidthEm, lineWidthEm(line));
        }
        double width = maxLineWidthEm * fontSize + 2 * PADDING_X;
        double height = lines.size() * fontSize * LINE_HEIGHT_FACTOR + 2 * PADDING_Y;
        return new Box((int) Math.round(width), (int) Math.round(height));
    }

    /** The width of one (already-wrapped, single) line in em units. */
    public static double lineWidthEm(String line) {
        double sum = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            sum += c < ADVANCE.length ? ADVANCE[c] : DEFAULT_ADVANCE;
        }
        return sum;
    }

    /**
     * Split {@code label} into display lines: first on {@code <br>} tags, then greedy word-wrap to
     * {@code maxLineLength} characters. Always returns at least one (possibly empty) line.
     */
    public static List<String> wrap(@Nullable String label, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        String text = label == null ? "" : label;
        for (String segment : text.split("(?i)<br\\s*/?>", -1)) {
            wrapSegment(segment, maxLineLength, lines);
        }
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private static void wrapSegment(String segment, int maxLineLength, List<String> out) {
        String trimmed = segment.trim();
        if (trimmed.isEmpty()) {
            out.add(""); // an explicit blank line (e.g. "a<br><br>b")
            return;
        }
        StringBuilder line = new StringBuilder();
        for (String word : trimmed.split("\\s+")) {
            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= maxLineLength) {
                line.append(' ').append(word);
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) out.add(line.toString());
    }
}
