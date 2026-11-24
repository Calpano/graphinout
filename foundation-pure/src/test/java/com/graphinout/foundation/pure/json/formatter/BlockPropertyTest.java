package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.bridge.Java9;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

class BlockPropertyTest {

    private static BlockProperty property(String key) {
        BlockContainer array = BlockContainer.createArrayBlock();
        array.children.add(new BlockValue("1"));
        array.children.add(new BlockValue("2"));
        return new BlockProperty(key, array);
    }

    @Test
    void fitsOnOneLineByDefault() {
        FormatterConfig config = FormatterConfig.of(80, Java9.Set.of(), false);
        assertThat(property("a").toTile(config, false).lines()).containsExactly("\"a\": [ 1, 2 ]");
    }

    @Test
    void forceMultiLineParameterIsRespected() {
        FormatterConfig config = FormatterConfig.of(80, Java9.Set.of(), false);
        assertThat(property("a").toTile(config, true).lines()) //
                .isEqualTo(property("b").toTile(FormatterConfig.of(80, Java9.Set.of("b"), false), false).lines().stream() //
                        .map(line -> line.replace("\"b\"", "\"a\"")).collect(java.util.stream.Collectors.toList()));
        assertThat(property("a").toTile(config, true).lines()).isEqualTo(Arrays.asList("\"a\":", "  [ 1,", "    2", "  ]"));
    }

}
