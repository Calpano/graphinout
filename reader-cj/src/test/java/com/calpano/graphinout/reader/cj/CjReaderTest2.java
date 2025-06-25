package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.reader.ContentError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class CjReaderTest2 extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new ConnectedJsonReader());
    }


    protected List<ContentError> expectedErrors(String resourceName) {
//        if(resourceName.endsWith("no-nodes.tgf")) {
//            return Arrays.asList( new ContentError(ContentError.ErrorLevel.Warn ,"No nodes found", null));
//        }
        return Collections.emptyList();
    }

}
