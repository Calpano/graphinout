package com.graphinout.base.input;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingleInputSourceTest {

    /** Counts how often a handed-out stream was closed. */
    static class CountingSource extends ByteArrayInputSource {
        final AtomicInteger closed = new AtomicInteger();

        CountingSource(byte[] bytes) {
            super("counting", bytes);
        }

        @Override
        public InputStream inputStream() {
            return new ByteArrayInputStream(bytes()) {
                @Override
                public void close() throws IOException {
                    closed.incrementAndGet();
                    super.close();
                }
            };
        }
    }

    static class FailingSource extends ByteArrayInputSource {
        FailingSource() {
            super("failing", new byte[0]);
        }

        @Override
        public InputStream inputStream() throws IOException {
            throw new IOException("boom");
        }
    }

    @Test
    void readsAsciiAndMultiByteUtf8() throws IOException {
        String s = "abc äöü € 𝄞";
        assertThat(SingleInputSource.of(s.getBytes(StandardCharsets.UTF_8)).readContentAsUtf8String()).isEqualTo(s);
    }

    @Test
    void readsEmptyInput() throws IOException {
        assertThat(SingleInputSource.of(new byte[0]).readContentAsUtf8String()).isEmpty();
    }

    @Test
    void replacesMalformedBytes() throws IOException {
        byte[] bytes = {'a', (byte) 0xFF, 'b'};
        assertThat(SingleInputSource.of(bytes).readContentAsUtf8String()).isEqualTo("a�b");
    }

    @Test
    void keepsBom() throws IOException {
        byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'x'};
        assertThat(SingleInputSource.of(bytes).readContentAsUtf8String()).isEqualTo("﻿x");
    }

    @Test
    void closesStreamAndCanReadTwice() throws IOException {
        CountingSource source = new CountingSource("hi".getBytes(StandardCharsets.UTF_8));
        assertThat(source.readContentAsUtf8String()).isEqualTo("hi");
        assertThat(source.closed.get()).isEqualTo(1);
        assertThat(source.readContentAsUtf8String()).isEqualTo("hi");
        assertThat(source.closed.get()).isEqualTo(2);
    }

    @Test
    void checkedVariantPropagatesIOException() {
        IOException e = assertThrows(IOException.class, () -> new FailingSource().readContentAsUtf8String());
        assertThat(e).hasMessageThat().isEqualTo("boom");
    }

    @Test
    void uncheckedVariantWrapsIOException() {
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> new FailingSource().getContentAsUtf8String());
        assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("boom");
    }

}
