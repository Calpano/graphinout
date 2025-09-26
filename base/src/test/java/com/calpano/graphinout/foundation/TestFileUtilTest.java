package com.calpano.graphinout.foundation;

import org.junit.jupiter.api.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

class TestFileUtilTest {

    @Test
    void test() {
        File f = new File("test.txt");
        File m = TestFileUtil.tagFile(f, "meta");
        assertThat(m).isEqualTo(new File("test--META.txt"));
    }

}
