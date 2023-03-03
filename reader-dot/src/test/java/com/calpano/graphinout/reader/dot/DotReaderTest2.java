package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.reader.GioReader;

import java.util.Arrays;
import java.util.List;

class DotReaderTest2 extends AbstractReaderTest {
    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new DotTextReader());
    }
}
