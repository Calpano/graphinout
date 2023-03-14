package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.basetest.AbstractReaderTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DotReaderTest2 extends AbstractReaderTest {
    protected List<ContentError> expectedErrors(String resourceName) {
        if (resourceName.endsWith("Call_Graph.gv")) {
            return Arrays.asList(new ContentError(ContentError.ErrorLevel.Error, "at line 35:22 extraneous input ',' expecting {'{', '}', GRAPH, NODE, EDGE, SUBGRAPH, NUMBER, STRING, ID, HTML_STRING}", null));
        }
        if(resourceName.endsWith("libinput-stack-xorg.gv")) {
            // TODO
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new DotTextReader());
    }
}