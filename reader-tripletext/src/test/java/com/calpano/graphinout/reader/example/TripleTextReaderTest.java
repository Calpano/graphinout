package com.calpano.graphinout.reader.example;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripleTextReaderTest extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new TripleTextReader());
    }

}
