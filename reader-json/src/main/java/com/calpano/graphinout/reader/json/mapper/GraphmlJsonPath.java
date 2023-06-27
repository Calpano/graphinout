package com.calpano.graphinout.reader.json.mapper;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

public class GraphmlJsonPath implements  PathBuilder{

    private final SingleInputSource mapperSource;
    private GraphmlJsonMapper jsonMapper;

    public GraphmlJsonPath(InputSource mapperSource) throws IOException {
        this.mapperSource = (SingleInputSource) mapperSource;
        loadMapperFile();
    }

    private void loadMapperFile() throws IOException {
        byte[] jsonData = new byte[this.mapperSource.inputStream().available()];
        this.mapperSource.inputStream().read(jsonData);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonMapper = objectMapper.readValue(jsonData, GraphmlJsonMapper.class);
    }

    @Override
    public String getId() {
        return jsonMapper.getId();
    }

    @Override
    public String getLabel() {
        return jsonMapper.getLabel();
    }

    @Override
    public PairValue<String> findAll() {
        return jsonMapper.findAll();
    }

    @Override
    public PairValue<String> findAllId() {
          return jsonMapper.findAllId();
    }

    @Override
    public PairValue<String> findById(String id) {
        return jsonMapper.findById(id);
    }

    @Override
    public PairValue<String> findById(Integer id) {
        return jsonMapper.findById(id);
    }

    @Override
    public PairValue<String> findByLabel(String label) {
        return jsonMapper.findByLabel(label);
    }

    @Override
    public  Set<PairValue<?>> findLink(String id) {
        return jsonMapper.findLink(id);
    }

    @Override
    public  Set<PairValue<?>>  findLink(Integer id) {
        return jsonMapper.findLink(id);
    }
}
