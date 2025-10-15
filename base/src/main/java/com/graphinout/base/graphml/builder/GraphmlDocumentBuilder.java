package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.IGraphmlDescription;
import com.graphinout.base.graphml.impl.GraphmlDocument;

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
