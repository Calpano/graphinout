package com.graphinout.testdata;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TestFileProviderTest {

    @Test
    void testResourceLoading() {
        // Always available: in-repo JSON resources and in-memory JSON5 inputs
        Truth.assertThat(TestFileProvider.getAllTestResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.jsonResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.json5InputSources()).isNotEmpty();

        // These resource types only exist in the external graph-test-data repo
        assumeTrue(TestFileUtil.externalRoot() != null, "external graph-test-data not available");
        Truth.assertThat(TestFileProvider.graphmlResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.xmlResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.cjResourcesCanonical()).isNotEmpty();
    }

}
