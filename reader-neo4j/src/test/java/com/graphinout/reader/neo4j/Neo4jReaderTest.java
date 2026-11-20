package com.graphinout.reader.neo4j;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;

import java.util.List;

/**
 * Standard test for Neo4j reader
 */
class Neo4jReaderTest extends AbstractReaderTest {


    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new Neo4jReader());
    }

}
