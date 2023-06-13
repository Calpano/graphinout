package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.FilesMultiInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class JsonReaderTest {

    @Test
    void test() throws IOException {
        FilesMultiInputSource dataAndMapping = new FilesMultiInputSource() //
                .withFile("data", new File("./src/test/resources/boardgames_40.json")) //
                .withFile("mapping", new File("./src/test/resources/json-mapper/json-mapper-1.json") //
        );
        JsonReader jsonReader = new JsonReader();
        InMemoryOutputSink out = new InMemoryOutputSink();
        GioWriter w = ReaderTests.createWriter(out, true, true, true);
        jsonReader.read(dataAndMapping, w);
    }

}
