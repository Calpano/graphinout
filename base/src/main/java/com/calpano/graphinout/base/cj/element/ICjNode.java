package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjNode extends ICjNodeProperties {

    @Nullable
    ICjLabel label();

    List<ICjPort> ports();

    @Nullable
    JsonNode data();

    List<ICjGraph> graphs();

}
