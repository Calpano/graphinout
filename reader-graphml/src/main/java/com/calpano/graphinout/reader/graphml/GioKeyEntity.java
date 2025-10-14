package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

public class GioKeyEntity extends AbstractGraphmlEntity<GioKey> implements GraphmlEntity<GioKey> {

    private final GioKey gioKey;

    public GioKeyEntity(GioKey gioKey) {
        this.gioKey = gioKey;
    }

    public void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if (GraphmlElements.DESC.equals(graphmlEntity.getName()))
            gioKey.setDescription(((GioDescriptionEntity) graphmlEntity).getEntity().getDescription());
        if (GraphmlElements.DEFAULT.equals(graphmlEntity.getName())) {
            String string = ((GioDefaultEntity) graphmlEntity).getEntity().toString();
            gioKey.setDefaultValue(XmlFragmentString.ofPlainText(string));
        }


    }

    @Override
    public GioKey getEntity() {
        return gioKey;
    }

    @Override
    public String getName() {
        return GraphmlElements.KEY;
    }


}
