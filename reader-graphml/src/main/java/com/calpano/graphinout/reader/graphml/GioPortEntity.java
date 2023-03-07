package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioPort;

public class GioPortEntity extends AbstractGraphmlEntity<GioPort> implements GraphmlEntity<GioPort>{
    private final GioPort gioPort;

    public GioPortEntity(GioPort gioPort) {
        this.gioPort = gioPort;
    }

    @Override
    public GioPort getEntity() {
        return gioPort;
    }

    @Override
    public String getName() {
        return GraphmlConstant.PORT_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
    }

    @Override
    public void addData(String data) {
    }

}
