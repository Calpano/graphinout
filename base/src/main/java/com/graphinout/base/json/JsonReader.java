package com.graphinout.base.json;

import com.graphinout.base.input.InputSource;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Reads JSON from an {@link com.graphinout.base.input.InputSource} and emits events to a
 *  {@link com.graphinout.foundation.pure.json.writer.JsonWriter}.
 */
public interface JsonReader {

    Logger log = getLogger(JsonReader.class);

    /**
     * Read JSON as a stream.
     *
     * @param inputSource to read
     * @param stream      receives the JSON events
     * @throws IOException can happen
     */
    void read(InputSource inputSource, JsonWriter stream) throws IOException;

}
