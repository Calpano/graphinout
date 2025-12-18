package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.foundation.pure.json.writer.impl.LoggingJsonWriter;
import com.graphinout.foundation.pure.log.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

class JsonParserTest {

    private static final Logger log = getLogger(JsonParserTest.class);

    @Test
    void test() {
        String json = "{\"baseDir\":\"\",\"expenses\":[]}";
        LoggerFactory.logSink(log::info);
        JsonParser.parse(json, new LoggingJsonWriter(LoggingJsonWriter.Output.Log));
    }

}
