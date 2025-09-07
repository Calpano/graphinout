package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjPort extends ICjPortProperties {

    @Nullable
    ICjLabel label();

    List<ICjPort> ports();

    @Nullable
    JsonNode data();

}
