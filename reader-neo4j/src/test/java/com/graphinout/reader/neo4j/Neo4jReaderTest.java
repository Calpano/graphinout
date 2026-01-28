package com.graphinout.reader.neo4j;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;

import java.util.Collections;
import java.util.List;

/**
 * Standard test for Neo4j reader
 */
class Neo4jReaderTest extends AbstractReaderTest {

    protected List<ContentError> expectedErrors(String resourceName) {
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new Neo4jReader());
    }

}
