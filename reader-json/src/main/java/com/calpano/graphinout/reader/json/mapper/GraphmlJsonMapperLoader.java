package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class GraphmlJsonMapperLoader {

    private final SingleInputSource mapperSource;
    private GraphmlJsonMapper graphmlJsonMapper;

    public GraphmlJsonMapperLoader(InputSource mapperSource) throws IOException {
        this.mapperSource = (SingleInputSource) mapperSource;
        loadMapperFile();
    }

    private void loadMapperFile() throws IOException {
        byte[] jsonData = new byte[this.mapperSource.inputStream().available()];
        this.mapperSource.inputStream().read(jsonData);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        graphmlJsonMapper = objectMapper.readValue(jsonData, GraphmlJsonMapper.class);
    }
    public final GraphmlJsonMapper getMapper(){
        return graphmlJsonMapper;
    }

}
