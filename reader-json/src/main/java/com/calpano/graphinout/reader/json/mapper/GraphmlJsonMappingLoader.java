package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class GraphmlJsonMappingLoader {

    public static GraphmlJsonMapping loadMapping(SingleInputSource mapperSource) throws IOException {
        // TODO improve I/O handling when mapperSource is a more complex input
        byte[] jsonData = new byte[mapperSource.inputStream().available()];
        mapperSource.inputStream().read(jsonData);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper.readValue(jsonData, GraphmlJsonMapping.class);
    }

}
