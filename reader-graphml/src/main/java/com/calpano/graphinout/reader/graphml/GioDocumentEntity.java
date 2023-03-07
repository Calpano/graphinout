package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;

import java.util.ArrayList;

public class GioDocumentEntity extends AbstractGraphmlEntity<GioDocument> implements GraphmlEntity<GioDocument> {
    private final GioDocument gioDocument;

    public GioDocumentEntity(GioDocument gioDocument) {
        this.gioDocument = gioDocument;
    }

    @Override
    public void addData(String data) {

    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {

        if (graphmlEntity instanceof GioDescriptionEntity g) {
            gioDocument.setDescription(g.getEntity().getDescription());
        } else if (graphmlEntity instanceof GioKeyEntity g) {
            if (gioDocument.getKeys() == null)
                gioDocument.setKeys(new ArrayList<>());
            gioDocument.getKeys().add(g.getEntity());
        } else if (graphmlEntity instanceof GioDataEntity g) {
            // TODO rasul implement
//            if (gioDocument.getDataList() == null)
//                gioDocument.setDataList(new ArrayList<>());
//            gioDocument.getDataList().add(g.getEntity());
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
        return GraphmlConstant.GRAPHML_ELEMENT_NAME;
    }

}
