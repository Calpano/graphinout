package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.impl.GraphmlDocument;

public class GraphmlDocumentBuilder extends GraphmlElementWithDescBuilder<GraphmlDocumentBuilder> {

    @Override
    public GraphmlDocument build() {
        return new GraphmlDocument(attributes, desc);
    }

    @Override
    public GraphmlDocumentBuilder desc(IGraphmlDescription desc) {
        super.desc(desc);
        return this;
    }

}
