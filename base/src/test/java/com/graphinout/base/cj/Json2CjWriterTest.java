package com.graphinout.base.cj;

import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.cj.writer.LoggingCjWriter;
import com.graphinout.foundation.json.writer.JsonWriter;
import org.junit.jupiter.api.Test;

public class Json2CjWriterTest {

    @Test
    void test() {
        JsonWriter jsonWriter = new Json2CjWriter(new LoggingCjWriter(false));
        jsonWriter.documentStart();
        // simulate { "data": { "foo":  "bar" } }
        jsonWriter.objectStart();
        jsonWriter.onKey("data");
        jsonWriter.objectStart();
        jsonWriter.onKey("foo");
        jsonWriter.onString("bar");
        jsonWriter.objectEnd();
        jsonWriter.objectEnd();
        jsonWriter.documentEnd();
    }

}
