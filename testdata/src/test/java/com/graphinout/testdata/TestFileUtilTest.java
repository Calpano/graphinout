package com.graphinout.testdata;

import com.google.common.truth.Truth;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

class TestFileUtilTest {

    /**
     * {@link TestFileUtil#file(Resource)} and {@link TestFileProvider.TestResource#asFile()} are only
     * meaningful for a resource that lives on the filesystem; for one packaged inside a jar they return
     * {@code null} by contract.
     *
     * <p>This used to be {@code @Disabled("Cannot run in pipeline")}, because it asserted
     * {@code isNotNull()} unconditionally — true when tests run against exploded {@code target/classes}
     * (local dev), false in a pipeline where the resource comes from a jar. Disabling it meant the path
     * assertions below never ran ANYWHERE, including locally. Asserting the real contract — a File when
     * filesystem-backed, {@code null} when jarred — makes the test correct in both environments instead
     * of skipped in both.
     */
    @Test
    void test() {
        String resourcePath = "xml/plain-xml/minimal.xml";
        Resource resource = TestFileUtil.resource(resourcePath);
        Truth.assertThat(resource).isNotNull();
        Truth.assertThat(resource.getPath()).isEqualTo(resourcePath);

        TestFileProvider.TestResource tr = TestFileProvider.TestResource.testResource(resource);
        Truth.assertThat(tr).isNotNull();
        Truth.assertThat(tr.resource()).isEqualTo(resource);
        Truth.assertThat(tr.asPath()).isEqualTo(resourcePath);

        boolean packagedInJar = resource.getURI().toString().startsWith("jar");
        File file = TestFileUtil.file(resource);
        if (packagedInJar) {
            Truth.assertThat(file).isNull();
            Truth.assertThat(tr.asFile()).isNull();
        } else {
            Truth.assertThat(file).isNotNull();
            Truth.assertThat(file.exists()).isTrue();
            Truth.assertThat(tr.asFile()).isEqualTo(file);
        }
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
