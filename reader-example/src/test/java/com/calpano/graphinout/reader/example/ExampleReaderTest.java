package com.calpano.graphinout.reader.example;

import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.basetest.AbstractReaderTest;

import java.util.Arrays;
import java.util.List;

class ExampleReaderTest extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new ExampleReader());
    }

}