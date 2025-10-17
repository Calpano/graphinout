package com.graphinout.base.cj;

import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.graphinout.foundation.json.stream.JsonWriter;
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
