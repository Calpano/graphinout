package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;

public interface ICjEndpoint extends ICjEndpointProperties {

    @Nullable
    ICjData data();

}
