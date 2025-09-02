package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;

public interface IGraphmlDocument extends IGraphmlElementWithDesc {

    static GraphmlDocumentBuilder builder() {
        return new GraphmlDocumentBuilder();
    }

    @Override
    default String tagName() {
        return GraphmlElements.GRAPHML;
    }


}
