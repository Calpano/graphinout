package com.calpano.graphinout.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GioEngineCoreTest {

    @Test
    void testServieLoading() {
        GioEngineCore core = new GioEngineCore();
        assertFalse(core.readers().isEmpty());
    }

}