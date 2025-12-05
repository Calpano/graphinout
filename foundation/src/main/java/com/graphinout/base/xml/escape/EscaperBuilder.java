package com.graphinout.base.xml.escape;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.graphinout.base.xml.escape.Preconditions.checkNotNull;

/**
 * A builder for simple, fast escapers.
 *
 * <p>Typically an escaper needs to deal with the escaping of high valued characters or code
 * points. In these cases it is necessary to extend either {@link ArrayBasedCharEscaper} or
 * {@code ArrayBasedUnicodeEscaper} to provide the desired behavior. However this builder is suitable for creating
 * escapers that replace a relative small set of characters.
 *
 * @author David Beaumont
 * @since 15.0
 */
public final class EscaperBuilder {

    private final Map<Character, String> replacementMap = new HashMap<>();
    private char safeMin = Character.MIN_VALUE;
    private char safeMax = Character.MAX_VALUE;
    @Nullable private String unsafeReplacement = null;

    // The constructor is exposed via the builder() method above.
    EscaperBuilder() {}

    /**
     * Sets the safe range of characters for the escaper. Characters in this range that have no explicit replacement are
     * considered 'safe' and remain unescaped in the output. If {@code safeMax < safeMin} then the safe range is empty.
     *
     * @param safeMin the lowest 'safe' character
     * @param safeMax the highest 'safe' character
     * @return the builder instance
     */
    public EscaperBuilder setSafeRange(char safeMin, char safeMax) {
        this.safeMin = safeMin;
        this.safeMax = safeMax;
        return this;
    }

    /**
     * Sets the replacement string for any characters outside the 'safe' range that have no explicit replacement. If
     * {@code unsafeReplacement} is {@code null} then no replacement will occur, if it is {@code ""} then the unsafe
     * characters are removed from the output.
     *
     * @param unsafeReplacement the string to replace unsafe characters
     * @return the builder instance
     */
    public EscaperBuilder setUnsafeReplacement(@Nullable String unsafeReplacement) {
        this.unsafeReplacement = unsafeReplacement;
        return this;
    }

    /**
     * Adds a replacement string for the given input character. The specified character will be replaced by the given
     * string whenever it occurs in the input, irrespective of whether it lies inside or outside the 'safe' range.
     *
     * @param c           the character to be replaced
     * @param replacement the string to replace the given character
     * @return the builder instance
     * @throws NullPointerException if {@code replacement} is null
     */
    public EscaperBuilder addEscape(char c, String replacement) {
        checkNotNull(replacement);
        // This can replace an existing character (the builder is re-usable).
        replacementMap.put(c, replacement);
        return this;
    }

    /** Returns a new escaper based on the current state of the builder. */
    public Escaper build() {
        return new ArrayBasedCharEscaper(replacementMap, safeMin, safeMax) {
            @Nullable
            private final char[] replacementChars =
                    unsafeReplacement != null ? unsafeReplacement.toCharArray() : null;

            @Override
            @Nullable
            protected char[] escapeUnsafe(char c) {
                return replacementChars;
            }
        };
    }

}
