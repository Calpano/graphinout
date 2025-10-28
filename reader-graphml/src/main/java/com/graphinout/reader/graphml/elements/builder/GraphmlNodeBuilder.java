package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlLocator;
import com.graphinout.reader.graphml.elements.impl.GraphmlNode;

import javax.annotation.Nullable;
import java.util.Map;

public class GraphmlNodeBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlNodeBuilder> implements ILocatorBuilder {

    private @Nullable IGraphmlLocator locator;

    @Override
    public GraphmlNodeBuilder attributes(@Nullable Map<String, String> attributes) {
        super.attributes(attributes);
        return this;
    }

    @Override
    public GraphmlNode build() {
        return new GraphmlNode(attributes, id, desc, locator);
    }

    @Override
    public GraphmlNodeBuilder locator(IGraphmlLocator locator) {
        this.locator = locator;
        return this;
    }

    @Nullable
    @Override
    public IGraphmlLocator locator() {
        return locator;
    }

}
