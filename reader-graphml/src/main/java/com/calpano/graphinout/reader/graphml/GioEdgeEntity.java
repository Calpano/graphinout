package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioElementWithDescription;

public class GioEdgeEntity implements  GraphmlEntity<GioEdge>{
    private final GioEdge gioEdge;

    public GioEdgeEntity(GioEdge gioEdge) {
        this.gioEdge = gioEdge;
    }

    @Override
    public GioEdge getEntity() {
        return gioEdge;
    }

    @Override
    public String getName() {
        return GraphmlConstant.EDGE_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if(graphmlEntity.getEntity() instanceof GioElementWithDescription g){
            gioEdge.setDescription(g.getDescription());
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
