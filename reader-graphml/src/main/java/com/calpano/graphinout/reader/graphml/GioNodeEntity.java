package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.graphml.GraphmlElements;

public class GioNodeEntity extends AbstractGraphmlEntity<GioNode> implements GraphmlEntity<GioNode> {

    private final GioNode gioNode;

    public GioNodeEntity(GioNode gioNode) {
        this.gioNode = gioNode;
    }

    public void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if (graphmlEntity.getEntity() instanceof GioElementWithDescription g) {
            gioNode.setDescription(g.getDescription());
        } else {
            throw new RuntimeException("Graphml has not " + graphmlEntity.getName() + " element.");
        }
    }

    @Override
    public GioNode getEntity() {
        return gioNode;
    }

    @Override
    public String getName() {
        return GraphmlElements.NODE;
    }


}
