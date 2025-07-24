package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlData;

import java.util.Set;

public interface IGraphmlData extends IGraphmlElement {

    String ATTRIBUTE_KEY = "key";
    String ATTRIBUTE_ID = "id";

    static GraphmlData.GraphmlDataBuilder builder() {
        return new GraphmlData.GraphmlDataBuilder();
    }

    String id();
    String key();
    String value();
    boolean isRawXml();

    default Set<String> builtInAttributes() {
        return Set.of(ATTRIBUTE_KEY, ATTRIBUTE_ID);
    }

    @Override
    default String tagName() {
        return "data";
    }
}
