package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.input.FilesMultiInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class JsonReaderTest {

    @Test
    @Disabled("The file is invalid. 280 Edge-node-id exists without existing node")
    void test_40() throws IOException {
        FilesMultiInputSource dataAndMapping =
                new FilesMultiInputSource() //
                        .withFile(JsonReader.DATA, new File("./src/test/resources/boardgames_40.json")) //
                        .withFile(JsonReader.MAPPING, new File("./src/test/resources/json-mapper/json-mapper-1.json") //
                        );
        JsonReader jsonReader = new JsonReader();
        InMemoryOutputSink out = new InMemoryOutputSink();
        GioWriter w = ReaderTests.createWriter(out, true, true, true);
        jsonReader.read(dataAndMapping, w);
    }

}
