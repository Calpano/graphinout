package com.graphinout.base;

import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Location;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractReaderTest {

    private static final Logger log = getLogger(AbstractReaderTest.class);

    protected List<ContentError> expectedErrors(Resource resourceName) {
        if (resourceName.getPath().endsWith("graph1_test.graphml"))
            return
                    Arrays.asList(
                            new ContentError(
                                    ContentError.ErrorLevel.Error,
                                    "Edge [GraphmlEdge(id=e1, directed=true, sourceId=n0, targetId=n4, sourcePortId=null, targetPortId=null)] references to a non-existent node ID: 'n4'",
                                    new Location(68, 11)
                            ));

        return Collections.emptyList();
    }

    protected abstract List<GioReader> readersToTest();

    @Test
    void testWithAllResources() {
        List<GioReader> gioReaders = readersToTest();
        for (GioReader gioReader : gioReaders) {
            ReaderTests.testWithAllResource(gioReader, this::expectedErrors);
        }
    }

}
