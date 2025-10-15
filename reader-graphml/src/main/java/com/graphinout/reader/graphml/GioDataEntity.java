package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.graphml.GraphmlElements;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;

public class GioDataEntity extends AbstractGraphmlEntity<GioData> implements GraphmlEntity<GioData> {

    private final GioData gioData;

    public GioDataEntity(GioData gioData) {
        this.gioData = gioData;
    }

    @Override
    public void addCharacters(String characters) {
        if (gioData.getValue() != null) {
            // 'append'
            String xmlValue = gioData.getValue().rawXml();
            xmlValue += characters;
            XmlFragmentString xmlFrag = XmlFragmentString.of(xmlValue, gioData.getValue().xmlSpace());
            gioData.setXmlValue(xmlFrag);
        } else {
            XmlFragmentString xmlFrag = XmlFragmentString.of(characters, XML.XmlSpace.default_);
            gioData.setXmlValue(xmlFrag);
        }
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
