package com.calpano.graphinout.foundation.json;

import com.calpano.graphinout.foundation.input.InputSource;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

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
