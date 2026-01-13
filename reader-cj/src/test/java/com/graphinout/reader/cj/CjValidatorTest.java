package com.graphinout.reader.cj;

import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.input.ContentError;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.base.TestFileUtil2.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CjValidatorTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test valid CJ files")
    void test_validCj(String displayPath, Resource resource) throws Exception {
        SingleInputSourceOfString inputSource = inputSource(resource);
        String cjJson = inputSource.getContentAsUtf8String();

        List<ContentError> errorList = new ArrayList<>();
        CjValidator.validateCj(cjJson, errorList);
        assertThat(errorList).isEmpty();
    }

    @Test
    void validateUnicode_emptyString_noErrors() {
        assertTrue(CjValidator.validateUnicode("").isEmpty());
    }

    @Test
    void validateUnicode_nullString_noErrors() {
        assertTrue(CjValidator.validateUnicode(null).isEmpty());
    }

    @Test
    void validateUnicode_unpairedHighSurrogate_error() {
        // Unpaired high surrogate at the end
        assertEquals(1, CjValidator.validateUnicode("abc\uD800").size());
        assertEquals("Unicode Error: Unpaired high surrogate at end of string at index 3", CjValidator.validateUnicode("abc\uD800").getFirst().message());

        // Unpaired high surrogate in the middle
        assertEquals(1, CjValidator.validateUnicode("abc\uD800def").size());
        assertEquals("Unicode Error: Unpaired high surrogate at index 3", CjValidator.validateUnicode("abc\uD800def").getFirst().message());
    }

    @Test
    void validateUnicode_unpairedLowSurrogate_error() {
        // Unpaired low surrogate
        assertEquals(1, CjValidator.validateUnicode("abc\uDC00").size());
        assertEquals("Unicode Error: Unpaired low surrogate at index 3", CjValidator.validateUnicode("abc\uDC00").getFirst().message());

        // Low surrogate followed by a non-surrogate
        assertEquals(1, CjValidator.validateUnicode("abc\uDC00d").size());
        assertEquals("Unicode Error: Unpaired low surrogate at index 3", CjValidator.validateUnicode("abc\uDC00d").getFirst().message());
    }

    @Test
    void validateUnicode_validString_noErrors() {
        assertTrue(CjValidator.validateUnicode("Hello World!").isEmpty());
        assertTrue(CjValidator.validateUnicode("你好世界").isEmpty());
        assertTrue(CjValidator.validateUnicode("😊").isEmpty()); // Single emoji (valid surrogate pair)
    }

}
