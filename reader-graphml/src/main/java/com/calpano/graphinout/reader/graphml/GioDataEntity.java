package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioData;

public class GioDataEntity extends AbstractGraphmlEntity<GioData> implements GraphmlEntity<GioData> {

    private final GioData gioData;

    public GioDataEntity(GioData gioData) {
        this.gioData = gioData;
    }

    @Override
    public void addCharacters(String characters) {
        StringBuilder builder;
        if (gioData.getValue() != null)
            gioData.setValue(gioData.getValue() + characters);
        else
            gioData.setValue(characters);

    }

    @Override
    public GioData getEntity() {
        return gioData;
    }

    @Override
    public String getName() {
        return GraphmlElements.DATA;
    }

}
