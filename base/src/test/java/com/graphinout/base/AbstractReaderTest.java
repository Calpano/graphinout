package com.graphinout.base;

import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

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

    /**
     * Error-level content errors a valid test resource is expected to produce; none by default. Deliberately broken
     * fixtures are tagged {@code --INVALID<marker>} in their file name and are not read by this test.
     */
    protected List<ContentError> expectedErrors(Resource resource) {
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
