package com.calpano.graphinout.base;

import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractReaderTest {

    private static final Logger log = getLogger(AbstractReaderTest.class);

    protected abstract List<GioReader> readersToTest();

    @Test
    void testWithAllResources() {
        List<GioReader> gioReaders = readersToTest();
        for (GioReader gioReader : gioReaders) {
            ReaderTests.testWithAllResource(gioReader);
        }
    }

}
