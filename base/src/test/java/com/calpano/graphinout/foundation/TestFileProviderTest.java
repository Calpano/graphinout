package com.calpano.graphinout.foundation;

import org.junit.jupiter.api.Test;

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

}
