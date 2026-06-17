package com.graphinout.testdata;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class TestFileProviderTest {

    @Test
    void testResourceLoading() {
        Truth.assertThat(TestFileProvider.getAllTestResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.graphmlResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.xmlResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.cjResourcesCanonical()).isNotEmpty();
        Truth.assertThat(TestFileProvider.jsonResources()).isNotEmpty();
        Truth.assertThat(TestFileProvider.json5InputSources()).isNotEmpty();
    }

}
