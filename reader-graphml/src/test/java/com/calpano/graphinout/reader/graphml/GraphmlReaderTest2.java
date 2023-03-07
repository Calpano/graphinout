package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GraphmlReaderTest2 extends AbstractReaderTest {

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new GraphmlReader());
    }

    @Test
    void test() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "graphin/graphml/samples/classements.graphml";
        List<ContentError> expectedErrors = Collections.emptyList();
        ReaderTests.testReadResourceToGraph(gioReader, resourcePath,expectedErrors);
    }

}