package com.graphinout.reader.pgformat;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;

import java.util.Collections;
import java.util.List;

/**
 * Standard test for PG-JSON reader
 */
class PgJsonReaderTest extends AbstractReaderTest {

    protected List<ContentError> expectedErrors(String resourceName) {
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new PgJsonReader());
    }

}
