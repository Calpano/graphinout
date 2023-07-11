package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class GraphmlJsonMappingLoader {

    private final SingleInputSource mapperSource;
    private GraphmlJsonMapping graphmlJsonMapping;

    public GraphmlJsonMappingLoader(InputSource mapperSource) throws IOException {
        this.mapperSource = (SingleInputSource) mapperSource;
        loadMapperFile();
    }

    private void loadMapperFile() throws IOException {
        byte[] jsonData = new byte[this.mapperSource.inputStream().available()];
        this.mapperSource.inputStream().read(jsonData);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        graphmlJsonMapping = objectMapper.readValue(jsonData, GraphmlJsonMapping.class);

    }
    public final GraphmlJsonMapping getMapper(){
        return graphmlJsonMapping;
    }

}
