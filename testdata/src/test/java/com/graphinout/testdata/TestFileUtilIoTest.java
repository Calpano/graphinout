package com.graphinout.testdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFileUtilIoTest {

    @TempDir
    Path tmp;

    @Test
    void writeUtf8CreatesMissingParentDirectories() throws IOException {
        Path file = tmp.resolve("a/b/c/out.txt");
        TestFileUtil.writeUtf8(file, "äöü €");
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("äöü €");
    }

    @Test
    void writeUtf8Overwrites() throws IOException {
        Path file = tmp.resolve("out.txt");
        TestFileUtil.writeUtf8(file, "first, longer content");
        TestFileUtil.writeUtf8(file, "second");
        assertThat(Files.readString(file)).isEqualTo("second");
    }

    @Test
    void readUtf8FromFileUrl() throws IOException {
        Path file = tmp.resolve("in.txt");
        Files.writeString(file, "x 𝄞", StandardCharsets.UTF_8);
        assertThat(TestFileUtil.readUtf8(file.toUri().toURL())).isEqualTo("x 𝄞");
    }

    @Test
    void readUtf8FromJarUrl() throws IOException {
        URL url = Test.class.getResource("Test.class");
        assertThat(url).isNotNull();
        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(TestFileUtil.readUtf8(url)).isNotEmpty();
    }

    @Test
    void readResourceUtf8() throws IOException {
        String content = TestFileUtil.readResourceUtf8(TestFileUtilIoTest.class, "/xml/plain-xml/minimal.xml");
        assertThat(content).contains("<");
    }

    @Test
    void readResourceUtf8MissingThrowsIOException() {
        IOException e = assertThrows(IOException.class,
                () -> TestFileUtil.readResourceUtf8(TestFileUtilIoTest.class, "/does/not/exist.txt"));
        assertThat(e).hasMessageThat().contains("/does/not/exist.txt");
    }

}
