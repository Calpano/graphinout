package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjDocument extends ICjDocumentProperties {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    JsonNode data();

    List<ICjGraph> graphs();

}
