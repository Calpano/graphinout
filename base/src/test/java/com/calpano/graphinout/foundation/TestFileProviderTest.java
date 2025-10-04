package com.calpano.graphinout.foundation;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class TestFileProviderTest {

    @Test
    void testResourceLoading() {
        assertThat(TestFileProvider.getAllTestResources()).isNotEmpty();
        assertThat(TestFileProvider.graphmlResources()).isNotEmpty();
        assertThat(TestFileProvider.xmlResources()).isNotEmpty();
        assertThat(TestFileProvider.cjResourcesCanonical()).isNotEmpty();
        assertThat(TestFileProvider.cjResourcesExtended()).isNotEmpty();
        assertThat(TestFileProvider.jsonResources()).isNotEmpty();
        assertThat(TestFileProvider.json5InputSources()).isNotEmpty();
    }

    @Test
    void test() throws Exception {

        List<Resource> resources = new ClassGraph().scan().getAllResources().stream().toList();
        Resource resource = resources.getFirst();
        URI uri = resource.getURI();

        Resource resourceExpected =  TestFileUtil.expectedResource(resource);

        System.out.println("uri ="+uri);

    }


}
