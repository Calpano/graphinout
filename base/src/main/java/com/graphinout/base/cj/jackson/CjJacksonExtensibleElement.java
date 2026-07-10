package com.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class CjJacksonExtensibleElement {

    private final Map<String, JsonNode> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void addAdditionalProperty(String key, JsonNode value) {
        additionalProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

}
