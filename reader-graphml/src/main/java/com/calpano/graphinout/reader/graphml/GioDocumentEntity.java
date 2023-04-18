package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioDocument;

import java.util.ArrayList;

public class GioDocumentEntity extends AbstractGraphmlEntity<GioDocument> implements GraphmlEntity<GioDocument> {
    private final GioDocument gioDocument;

    public GioDocumentEntity(GioDocument gioDocument) {
        this.gioDocument = gioDocument;
    }


    @Override
    public void addEntity(GraphmlEntity<?> graphmlEntity) {
        if (graphmlEntity instanceof GioDescriptionEntity g) {
            gioDocument.setDescription(g.getEntity().getDescription());
        } else {
            throw new RuntimeException("Graphml has not " + graphmlEntity.getName() + " element.");
        }
    }

    @Override
    public GioDocument getEntity() {
        return gioDocument;
    }

    @Override
    public String getName() {
        return GraphmlElement.GRAPHML;
    }

}
