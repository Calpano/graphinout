package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.impl.GraphmlKey;

import javax.annotation.Nullable;
import java.util.Map;

/** builds {@link com.calpano.graphinout.base.graphml.IGraphmlKey} */
public class GraphmlKeyBuilder extends GraphmlElementWithDescBuilder<GraphmlKeyBuilder> {

    private String id;
    private String attrName;
    private String attrType;
    private GraphmlKeyForType forType;
    private IGraphmlDefault defaultValue;

    public GraphmlKeyBuilder attrName(String attrName) {
        this.attrName = attrName;
        return this;
    }

    public GraphmlKeyBuilder attrType(String attrType) {
        this.attrType = attrType;
        return this;
    }

    @Override
    public GraphmlKeyBuilder attributes(@Nullable Map<String, String> attributes) {
        super.attributes(attributes);
        return this;
    }

    @Override
    public IGraphmlKey build() {
        return new GraphmlKey(attributes, id, desc, attrName, attrType, forType, defaultValue);
    }

    public GraphmlKeyBuilder defaultValue(IGraphmlDefault defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public GraphmlKeyBuilder desc(IGraphmlDescription desc) {
        super.desc(desc);
        return this;
    }

    public GraphmlKeyBuilder forType(GraphmlKeyForType forType) {
        this.forType = forType;
        return this;
    }

    public GraphmlKeyBuilder id(String id) {
        this.id = id;
        return this;
    }

}
