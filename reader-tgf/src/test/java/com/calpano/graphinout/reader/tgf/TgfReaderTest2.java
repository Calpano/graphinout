package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.basetest.AbstractReaderTest;
import com.calpano.graphinout.base.reader.GioReader;

import java.util.Arrays;
import java.util.List;

class TgfReaderTest2 extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new TgfReader());
    }

}