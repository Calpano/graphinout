package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.GraphmlDirection;
import com.graphinout.base.graphml.IGraphmlEdge;
import com.graphinout.base.graphml.IGraphmlElementWithId;
import com.graphinout.base.graphml.IGraphmlEndpoint;
import com.graphinout.base.graphml.impl.GraphmlEdge;
import com.graphinout.base.graphml.impl.GraphmlHyperEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class GraphmlHyperEdgeBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlHyperEdgeBuilder> {

    private final ArrayList<IGraphmlEndpoint> endpoints = new ArrayList<>();

    public GraphmlHyperEdgeBuilder addEndpoint(IGraphmlEndpoint gioEndpoint) {
        endpoints.add(gioEndpoint);
        return this;
    }

    public GraphmlHyperEdge build() {
//        if (endpoints.size() < 2)
//            throw new IllegalStateException("Require at least 2 endpoints in hyperedge, got " + endpoints.size());
        return new GraphmlHyperEdge(id, attributes, desc, endpoints);
    }

    public boolean isBiEdge() {
        return endpoints.size() == 2;
    }

    public GraphmlEdge toEdge() {
        assert isBiEdge();
        GraphmlEdgeBuilder builder = IGraphmlEdge.builder();
        if (attributes != null) {
            ifPresentAccept(attributes.get(IGraphmlElementWithId.ATTRIBUTE_ID), builder::id);
            Map<String, String> atts = new HashMap<>(attributes);
            atts.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
            builder.attributes(atts);
        }
        ifPresentAccept(desc, builder::desc);

        assert endpoints.size() == 2;
        IGraphmlEndpoint ep0 = endpoints.get(0);
        IGraphmlEndpoint ep1 = endpoints.get(1);
        assert ep0.type().isDirected() == ep1.type().isDirected();
        if (ep0.type().isDirected()) {
            // directed edge
            assert ep0.type() != ep1.type() : "dir must be different";
            IGraphmlEndpoint source = endpoints.stream().filter(e -> e.type() == GraphmlDirection.In).findFirst().orElseThrow();
            IGraphmlEndpoint target = endpoints.stream().filter(e -> e.type() == GraphmlDirection.Out).findFirst().orElseThrow();
            // IMPROVE set only if different from the default in the graph
            builder.directed(source.type() != GraphmlDirection.Undirected);
            builder.sourceId(source.node());
            builder.sourcePortId(source.port());
            builder.targetId(target.node());
            builder.targetPortId(target.port());
        } else {
            // undirected edge
            // IMPROVE set only if different from the default in the graph
            builder.directed(false);
            builder.sourceId(ep0.node());
            builder.sourcePortId(ep0.port());
            builder.targetId(ep1.node());
            builder.targetPortId(ep1.port());
        }

        return builder.build();
    }

}
