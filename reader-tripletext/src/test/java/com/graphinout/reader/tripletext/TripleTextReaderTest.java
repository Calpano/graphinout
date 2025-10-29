package com.graphinout.reader.tripletext;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

class TripleTextReaderTest extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new TripleTextReader());
    }

}
