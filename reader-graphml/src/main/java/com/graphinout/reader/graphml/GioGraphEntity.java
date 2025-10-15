package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.graphml.GraphmlElements;

public class GioGraphEntity extends AbstractGraphmlEntity<GioGraph> implements GraphmlEntity<GioGraph> {

    private final GioGraph gioGraph;


    public GioGraphEntity(GioGraph gioGraph) {
        this.gioGraph = gioGraph;
    }

    public void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }

    @Override
    public void addEntity(GraphmlEntity<?> graphmlEntity) {
        if (graphmlEntity instanceof GioDescriptionEntity g) {
            gioGraph.setDescription(g.getEntity().getDescription());
        } else {
            throw new RuntimeException("Graph has not " + graphmlEntity.getName() + " element.");
        }
    }

    @Override
    public GioGraph getEntity() {
        return gioGraph;
    }

    @Override
    public String getName() {
        return GraphmlElements.GRAPH;
    }


}
