package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithId;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphmlHyperEdgeBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlHyperEdgeBuilder> {

    private final ArrayList<IGraphmlEndpoint> endpoints = new ArrayList<>();

    public GraphmlHyperEdgeBuilder addEndpoint(IGraphmlEndpoint gioEndpoint) {
        endpoints.add(gioEndpoint);
        return this;
    }

    public GraphmlHyperEdge build() {
        if (endpoints.size() < 2)
            throw new IllegalStateException("Require at least 2 endpoints in hyperedge, got " + endpoints.size());
        return new GraphmlHyperEdge(id, attributes, desc, endpoints);
    }

    public boolean isBiEdge() {
        return endpoints.size() == 2;
    }

    public GraphmlEdge toEdge() {
        assert isBiEdge();

        GraphmlEdgeBuilder builder = IGraphmlEdge.builder();
        builder.id(attributes.get(IGraphmlElementWithId.ATTRIBUTE_ID));

        Map<String, String> atts = new HashMap<>(attributes);
        atts.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
        builder.attributes(atts);

        builder.desc(desc);
        IGraphmlEndpoint source = endpoints.stream().filter(e -> e.type() == GraphmlDirection.In).findFirst().orElse(endpoints.get(0));
        IGraphmlEndpoint target = endpoints.stream().filter(e -> e.type() == GraphmlDirection.Out).findFirst().orElse(endpoints.get(1));

        // FIXME set only if different from the default
        builder.directed(source.type() != GraphmlDirection.Undirected);
        builder.sourceId(source.id());
        builder.sourcePortId(source.port());
        builder.targetId(target.id());
        builder.targetPortId(target.port());
        return builder.build();
    }

}
