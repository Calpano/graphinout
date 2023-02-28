package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioData;

public class GioDataEntity implements  GraphmlEntity<GioData> {
    private final GioData gioData;

    public GioDataEntity(GioData gioData) {
        this.gioData = gioData;
    }

    @Override
    public GioData getEntity() {
        return gioData;
    }

    @Override
    public String getName() {
        return "data";
    }

    @Override
    public void addEntity(GraphmlEntity<?> graphmlEntity) {
      throw new RuntimeException("Data has not any inner element.");
    }

    @Override
    public void addData(String data) {
           StringBuilder builder ;
           if(gioData.getValue()!=null)
               gioData.setValue(gioData.getValue()+data);
           else
               gioData.setValue(data);

    }

    @Override
    public boolean mustSendToStream(String newElementName) {
        return false;
    }
}
