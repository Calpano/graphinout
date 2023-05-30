package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

public class JsonPath {

    private final SingleInputSource mapperSource;
    private JsonMapper jsonMapper;

    public JsonPath(InputSource mapperSource) throws IOException {
        this.mapperSource = (SingleInputSource) mapperSource;
        loadMapperFile();
    }

    private void loadMapperFile() throws IOException {
        byte[] jsonData = new byte[this.mapperSource.inputStream().available()];
        this.mapperSource.inputStream().read(jsonData);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        jsonMapper = objectMapper.readValue(jsonData, JsonMapper.class);
    }

    public Set<String> paths() {
        return jsonMapper.paths();
    }
}
