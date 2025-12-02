package com.graphinout.foundation.json.util;

import java.util.Set;

/**
 * @param maxWidth           maximal line length
 * @param forceMultiLineKeys for properties with this key, each direct member is 'forceMultiLine'
 * @param forceMultiLine     all direct children are forced on their own lines
 */
public record FormatterConfig(int maxWidth, Set<String> forceMultiLineKeys, boolean sortMaps) {

    public static FormatterConfig of(int maxLineLength, Set<String> forceMultiLineKeys, boolean sortMaps) {
        return new FormatterConfig(maxLineLength, forceMultiLineKeys, sortMaps);
    }

    public FormatterConfig withMaxWidth(int maxWidth) {
        return new FormatterConfig(maxWidth, forceMultiLineKeys, sortMaps);
    }

}
