package com.graphinout.base.cj.anonymize;

import org.jspecify.annotations.Nullable;

/**
 * The character-level anonymization rule used by {@link CjDocumentAnonymizer} to redact copyrightable
 * text while preserving its <em>shape</em>: word lengths, word breaks, spacing and punctuation stay
 * identical, so the anonymized graph still looks structurally like the original.
 *
 * <p>The rule, applied per Unicode code point so it also covers characters outside the BMP:
 * <ul>
 *   <li>an upper-case or title-case letter &rarr; {@code 'X'}</li>
 *   <li>a lower-case letter &rarr; {@code 'x'}</li>
 *   <li>any other letter (case-less scripts: CJK, Arabic, Hebrew, …) &rarr; {@code 'x'}
 *       — anonymized too, to be safe</li>
 *   <li>a digit &rarr; {@code '0'}</li>
 *   <li>everything else (whitespace, punctuation, symbols) &rarr; kept verbatim</li>
 * </ul>
 */
public final class Anonymizer {

    private Anonymizer() {}

    /** Apply the character rule to {@code s} (null in &rarr; null out). */
    public static @Nullable String text(@Nullable String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> sb.appendCodePoint(anonymizeCodePoint(cp)));
        return sb.toString();
    }

    private static int anonymizeCodePoint(int cp) {
        if (Character.isUpperCase(cp) || Character.isTitleCase(cp)) return 'X';
        if (Character.isLowerCase(cp)) return 'x';
        if (Character.isDigit(cp)) return '0';
        if (Character.isLetter(cp)) return 'x'; // case-less letters (e.g. CJK, Arabic)
        return cp; // whitespace, punctuation, symbols: keep
    }
}
