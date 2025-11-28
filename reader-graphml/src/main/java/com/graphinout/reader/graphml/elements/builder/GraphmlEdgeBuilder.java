package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.impl.GraphmlEdge;

import javax.annotation.Nullable;
import java.util.Map;

public class GraphmlEdgeBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlEdgeBuilder> implements IIdBuilder {

    private Boolean directed;
    private String sourceId;
    private String targetId;
    private String sourcePortId;
    private String targetPortId;

    @Override
    public GraphmlEdge build() {
        return new GraphmlEdge(attributes, id, directed, sourceId, targetId, sourcePortId, targetPortId, desc);
    }

    @Override
    public GraphmlEdgeBuilder desc(IGraphmlDescription desc) {
        super.desc(desc);
        return this;
    }

    public GraphmlEdgeBuilder directed(Boolean directed) {
        this.directed = directed;
        return this;
    }

    @Override
    public GraphmlEdgeBuilder attributes(@Nullable Map<String, String> attributes) {
        super.attributes(attributes);
        return this;
    }

    public GraphmlEdgeBuilder sourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public GraphmlEdgeBuilder sourcePortId(String sourcePortId) {
        this.sourcePortId = sourcePortId;
        return this;
    }

    public GraphmlEdgeBuilder targetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public GraphmlEdgeBuilder targetPortId(String targetPortId) {
        this.targetPortId = targetPortId;
        return this;
    }

}
