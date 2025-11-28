package com.graphinout.reader.example;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

class ExampleReaderTest extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new ExampleReader());
    }

}
