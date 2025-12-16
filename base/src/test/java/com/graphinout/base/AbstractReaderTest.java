package com.graphinout.base;

import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractReaderTest {

    public static Consumer<ContentError> createErrorHandlerOnLog( Logger slf4jLog) {
        return contentError -> {
            switch (contentError.getLevel()) {
                case Error -> slf4jLog.error(contentError.toString());
                case Warn -> slf4jLog.warn(contentError.toString());
                case Info -> slf4jLog.info(contentError.toString());
            }
        };
    }

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
