package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioKey;

public class GioKeyEntity implements GraphmlEntity<GioKey>{
    private final GioKey gioKey;

    public GioKeyEntity(GioKey gioKey) {
        this.gioKey = gioKey;
    }

    @Override
    public GioKey getEntity() {
        return gioKey;
    }

    @Override
    public String getName() {
        return GraphmlConstant.KEY_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if(GraphmlConstant.DESC_ELEMENT_NAME.equals(graphmlEntity.getName()))
            gioKey.setDescription(((GioDescriptionEntity) graphmlEntity).getEntity().getDescription());

    }

    @Override
    public void addData(String data) {

    }

    @Override
    public boolean mustSendToStream(String newElementName) {
        return false;
    }
}
