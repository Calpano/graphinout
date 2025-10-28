package com.graphinout.reader.cj;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.GioReader;
import com.graphinout.foundation.input.ContentError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class CjReaderTest2 extends AbstractReaderTest {

    protected List<ContentError> expectedErrors(String resourceName) {
//        if(resourceName.endsWith("no-nodes.tgf")) {
//            return Arrays.asList( new ContentError(ContentError.ErrorLevel.Warn ,"No nodes found", null));
//        }
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new ConnectedJsonReader());
    }

}
