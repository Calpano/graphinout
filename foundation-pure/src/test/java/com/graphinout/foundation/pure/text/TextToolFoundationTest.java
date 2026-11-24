package com.graphinout.foundation.pure.text;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextToolFoundationTest {

    @Test
    void padLeft() {
        assertThat(TextToolFoundation.padLeft("0", "7", 3)).isEqualTo("007");
        assertThat(TextToolFoundation.padLeft("ab", "x", 4)).isEqualTo("abax");
        assertThat(TextToolFoundation.padLeft("ab", "x", 5)).isEqualTo("ababx");
        assertThat(TextToolFoundation.padLeft("abc", "x", 2)).isEqualTo("ax");
        assertThat(TextToolFoundation.padLeft("0", "long", 2)).isEqualTo("long");
    }

    @Test
    void padRight() {
        assertThat(TextToolFoundation.padRight("0", "7", 3)).isEqualTo("700");
        assertThat(TextToolFoundation.padRight("ab", "x", 4)).isEqualTo("xaba");
        assertThat(TextToolFoundation.padRight("0", "long", 2)).isEqualTo("long");
    }

    @Test
    void emptyPaddingFailsOnlyIfPaddingIsNeeded() {
        assertThat(TextToolFoundation.padLeft("", "long", 2)).isEqualTo("long");
        assertThrows(IllegalArgumentException.class, () -> TextToolFoundation.padLeft("", "x", 2));
    }

}
