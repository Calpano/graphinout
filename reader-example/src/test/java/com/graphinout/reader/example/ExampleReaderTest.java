package com.graphinout.reader.example;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.GioReader;

import java.util.Arrays;
import java.util.List;

class ExampleReaderTest extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new ExampleReader());
    }

}
