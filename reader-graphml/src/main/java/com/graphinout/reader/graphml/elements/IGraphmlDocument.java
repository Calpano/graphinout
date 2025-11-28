package com.graphinout.reader.graphml.elements;

import com.graphinout.reader.graphml.elements.builder.GraphmlDocumentBuilder;

public interface IGraphmlDocument extends IGraphmlElementWithDesc {

    static GraphmlDocumentBuilder builder() {
        return new GraphmlDocumentBuilder();
    }

    @Override
    default String tagName() {
        return GraphmlElements.GRAPHML;
    }


}
