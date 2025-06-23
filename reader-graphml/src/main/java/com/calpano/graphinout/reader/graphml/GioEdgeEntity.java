package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.gio.GioEndpoint;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GioEdgeEntity extends AbstractGraphmlEntity<List<GioEndpoint>> implements GraphmlEntity<List<GioEndpoint>> {
    public final List<GioEndpoint> endpoints = new ArrayList<>();
    public @Nullable String id;
    public @Nullable String desc;

    public GioEdgeEntity() {
    }

    public void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }

    public void addEndpoint(GioEndpoint gioEndpoint) {
        this.endpoints.add(gioEndpoint);
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if (graphmlEntity.getEntity() instanceof GioElementWithDescription g) {
            this.desc = g.getDescription();
        }
    }

    public GioEdge buildEdge() {
        GioEdge.GioEdgeBuilder b = GioEdge.builder();
        if (id != null) b.id(id);
        if (desc != null) b.description(desc);
        b.endpoints(endpoints);
        return b.build();
    }

    @Override
    public List<GioEndpoint> getEntity() {
        return endpoints;
    }

    @Override
    public String getName() {
        return GraphmlElement.EDGE;
    }

    public boolean resultsInGraphmlElement(String graphmlElementName) {
        return getName().equals(GraphmlElement.EDGE) || getName().equals(GraphmlElement.HYPER_EDGE);
    }


}
