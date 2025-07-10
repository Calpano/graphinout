package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class GioExtensibleElement {

    private Map<String, JsonNode> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void addAdditionalProperty(String key, JsonNode value) {
        additionalProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

}
