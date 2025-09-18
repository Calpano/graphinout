package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public interface ICjDocument extends ICjDocumentProperties, ICjWithGraphs {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    ICjData data();

}
