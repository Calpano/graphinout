package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.gio.GioNode;

public class GioNodeEntity implements GraphmlEntity<GioNode>{
    private final GioNode gioNode;

    public GioNodeEntity(GioNode gioNode) {
        this.gioNode = gioNode;
    }

    @Override
    public GioNode getEntity() {
        return gioNode;
    }

    @Override
    public String getName() {
        return GraphmlConstant.NODE_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
    if(graphmlEntity.getEntity() instanceof GioElementWithDescription g){
        gioNode.setDescription(g.getDescription());
    }
    }

    @Override
    public void addData(String data) {

    }

    @Override
    public boolean mustSendToStream(String newElementName) {
        return false;
    }
}
