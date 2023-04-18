package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioGraph;

public class GioGraphEntity extends AbstractGraphmlEntity<GioGraph> implements GraphmlEntity<GioGraph> {

    private final GioGraph gioGraph;


    public GioGraphEntity(GioGraph gioGraph) {
        this.gioGraph = gioGraph;
    }

    @Override
    public GioGraph getEntity() {
        return gioGraph;
    }

    @Override
    public String getName() {
        return GraphmlElement.GRAPH;
    }

    @Override
    public void addEntity(GraphmlEntity<?> graphmlEntity) {
        if (graphmlEntity instanceof GioDescriptionEntity g) {
            gioGraph.setDescription(g.getEntity().getDescription());
        } else {
            throw new RuntimeException("Graph has not " + graphmlEntity.getName() + " element.");
        }
    }

    public void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }



}
