package com.graphinout.engine;

import com.graphinout.base.gio.GioFileFormat;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertWithMessage;

/** Every registered format id follows {@link GioFileFormat#ID_PATTERN}. */
class FormatIdTest {

    @Test
    void allFormatIdsMatchPattern() {
        List<String> invalid = new GioEngineCore().formats().keySet().stream()
                .map(GioFileFormat::id)
                .filter(id -> !id.matches(GioFileFormat.ID_PATTERN))
                .toList();
        assertWithMessage("format ids not matching %s", GioFileFormat.ID_PATTERN).that(invalid).isEmpty();
    }

}
