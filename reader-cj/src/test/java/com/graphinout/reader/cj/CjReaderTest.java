package com.graphinout.reader.cj;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;

import java.util.Collections;
import java.util.List;

/**
 * Standard test for all readers
 */
class CjReaderTest extends AbstractReaderTest {

    protected List<ContentError> expectedErrors(String resourceName) {
        // IMPROVE test files with errors and expect them here
//        if(resourceName.endsWith("no-nodes.tgf")) {
//            return Arrays.asList( new ContentError(ContentError.ErrorLevel.Warn ,"No nodes found", null));
//        }
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new ConnectedJsonReader());
    }

}
