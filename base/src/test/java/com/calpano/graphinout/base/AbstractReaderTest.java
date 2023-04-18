package com.calpano.graphinout.base;

import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractReaderTest {

    private static final Logger log = getLogger(AbstractReaderTest.class);

    protected List<ContentError> expectedErrors(String resourceName) {
        if (resourceName.equals("graphin/graphml/samples/graph1_test.graphml"))
            return
                    Arrays.asList(
                            new ContentError(
                                    ContentError.ErrorLevel.Error,
                                    "Edge [GraphmlEdge(id=e1, directed=true, sourceId=n0, targetId=n4, sourcePortId=null, targetPortId=null)] references to a non-existent node ID: 'n4'",
                                    new ContentError.Location(68, 11)
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
