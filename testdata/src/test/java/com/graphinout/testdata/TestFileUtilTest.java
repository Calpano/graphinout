package com.graphinout.testdata;

import com.google.common.truth.Truth;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

class TestFileUtilTest {

    @Test
    @Disabled("Cannot run in pipeline")
    void test() {
        String resourcePath = "xml/minimal.xml";
        Resource resource = TestFileUtil.resource(resourcePath);
        Truth.assertThat(resource).isNotNull();
        Truth.assertThat(resource.getPath()).isEqualTo(resourcePath);

        File file = TestFileUtil.file(resource);
        Truth.assertThat(file).isNotNull();

        TestFileProvider.TestResource tr = TestFileProvider.TestResource.testResource(resource);
        Truth.assertThat(tr).isNotNull();
        Truth.assertThat(tr.resource()).isEqualTo(resource);
        File f = tr.asFile();
        Truth.assertThat(f).isEqualTo(file);
        String path = tr.asPath();
        Truth.assertThat(path).isEqualTo(resourcePath);
    }

    @Test
    void testTaggedResource() {
        String path = "foo/bar/baz.buz/dingo.graphml.xml";
        String tag = "expected";
        Truth.assertThat(TestFileUtil.tagResourcePath(path, tag)).isEqualTo("foo/bar/baz.buz/dingo--EXPECTED.graphml.xml");
    }

    @Test
    void testX() {
        File f = new File("test.txt");
        File m = TestFileUtil.tagFile(f, "meta");
        Truth.assertThat(m).isEqualTo(new File("test--META.txt"));
    }

}
