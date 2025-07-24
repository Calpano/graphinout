package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioElementWithDescription;

public class GioDescriptionEntity extends AbstractGraphmlEntity<GioElementWithDescription> implements GraphmlEntity<GioElementWithDescription> {

    private GioElementWithDescription gioElementWithDescription = new GioElementWithDescription() {
    };

    @Override
    public void addCharacters(String characters) {
        String description = gioElementWithDescription.getDescription();
        gioElementWithDescription.setDescription((description == null ? "" : description) + characters);
    }

    @Override
    public GioElementWithDescription getEntity() {
        return gioElementWithDescription;
    }

    @Override
    public String getName() {
        return GraphmlElements.DESC;
    }

}
