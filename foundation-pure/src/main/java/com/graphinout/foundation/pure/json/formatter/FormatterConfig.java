package com.graphinout.foundation.pure.json.formatter;

import java.util.Objects;
import java.util.Set;

/**
 *
 */
public final class FormatterConfig {

    private final int maxWidth;
    private final Set<String> forceMultiLineKeys;
    private final boolean sortMaps;

    /**
     * @param maxWidth           maximal line length
     * @param forceMultiLineKeys for properties with this key, each direct member is 'forceMultiLine'
     * @param sortMaps           map properties are sorted by key
     */
    public FormatterConfig(int maxWidth, Set<String> forceMultiLineKeys, boolean sortMaps) {
        this.maxWidth = maxWidth;
        this.forceMultiLineKeys = forceMultiLineKeys;
        this.sortMaps = sortMaps;
    }

    public static FormatterConfig of(int maxLineLength, Set<String> forceMultiLineKeys, boolean sortMaps) {
        return new FormatterConfig(maxLineLength, forceMultiLineKeys, sortMaps);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FormatterConfig that = (FormatterConfig) obj;
        return this.maxWidth == that.maxWidth &&
                Objects.equals(this.forceMultiLineKeys, that.forceMultiLineKeys) &&
                this.sortMaps == that.sortMaps;
    }

    public Set<String> forceMultiLineKeys() {return forceMultiLineKeys;}

    @Override
    public int hashCode() {
        return Objects.hash(maxWidth, forceMultiLineKeys, sortMaps);
    }

    public int maxWidth() {return maxWidth;}

    public boolean sortMaps() {return sortMaps;}

    @Override
    public String toString() {
        return "FormatterConfig[" +
                "maxWidth=" + maxWidth + ", " +
                "forceMultiLineKeys=" + forceMultiLineKeys + ", " +
                "sortMaps=" + sortMaps + ']';
    }

    public FormatterConfig withMaxWidth(int maxWidth) {
        return new FormatterConfig(maxWidth, forceMultiLineKeys, sortMaps);
    }


}
