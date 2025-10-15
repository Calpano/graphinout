package com.graphinout.foundation;

import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

class TestFileUtilTest {

    @Test
    void test() {
        String resourcePath = "xml/minimal.xml";
        Resource resource = TestFileUtil.resource(resourcePath);
        assertThat(resource).isNotNull();
        assertThat(resource.getPath()).isEqualTo(resourcePath);

        File file = TestFileUtil.file(resource);
        assertThat(file).isNotNull();

        TestFileProvider.TestResource tr = TestFileProvider.TestResource.testResource(resource);
        assertThat(tr).isNotNull();
        assertThat(tr.resource()).isEqualTo(resource);
        File f = tr.asFile();
        assertThat(f).isEqualTo(file);
        String path = tr.asPath();
        assertThat(path).isEqualTo(resourcePath);
    }

    @Test
    void testTaggedResource() {
        String path = "foo/bar/baz.buz/dingo.graphml.xml";
        String tag = "expected";
        assertThat(TestFileUtil.tagResourcePath(path, tag)).isEqualTo("foo/bar/baz.buz/dingo--EXPECTED.graphml.xml");
    }

    @Test
    void testX() {
        File f = new File("test.txt");
        File m = TestFileUtil.tagFile(f, "meta");
        assertThat(m).isEqualTo(new File("test--META.txt"));
    }

}
