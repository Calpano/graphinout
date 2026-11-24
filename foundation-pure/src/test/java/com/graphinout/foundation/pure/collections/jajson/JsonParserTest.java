package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.foundation.pure.json.writer.impl.LoggingJsonWriter;
import com.graphinout.foundation.pure.log.LoggerFactory;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.input.Location;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.slf4j.LoggerFactory.getLogger;

class JsonParserTest {

    private static final Logger log = getLogger(JsonParserTest.class);

    @Test
    void test() {
        String json = "{\"baseDir\":\"\",\"expenses\":[]}";
        LoggerFactory.logSink(log::info);
        JsonParser.parse(json, new LoggingJsonWriter(LoggingJsonWriter.Output.Log));
    }

    private static Location errorLocation(String json) {
        return assertThrows(ContentErrorException.class, () -> JsonParser.parse(json)).location();
    }

    @Test
    void errorLocationHasLineAndColumn() {
        // the error is at the 'x' in line 3, column 8
        assertEquals(Location.of(3, 8), errorLocation("{\n  \"a\": 1,\n  \"b\": x\n}"));
        assertEquals(Location.of(3, 8), errorLocation("{\r\n  \"a\": 1,\r\n  \"b\": x\r\n}"));
        assertEquals(Location.of(3, 8), errorLocation("{\r  \"a\": 1,\r  \"b\": x\r}"));
        assertEquals(Location.of(1, 7), errorLocation("{\"b\": x}"));
    }

}
