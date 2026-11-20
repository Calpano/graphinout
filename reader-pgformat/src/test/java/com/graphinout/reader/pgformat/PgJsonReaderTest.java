package com.graphinout.reader.pgformat;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;

import java.util.List;

/**
 * Standard test for PG-JSON reader
 */
class PgJsonReaderTest extends AbstractReaderTest {


    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new PgJsonReader());
    }

}
