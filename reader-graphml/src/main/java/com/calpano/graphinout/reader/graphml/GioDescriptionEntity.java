package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrDefault;

public class GioDescriptionEntity extends AbstractGraphmlEntity<GioElementWithDescription> implements GraphmlEntity<GioElementWithDescription> {

    private final GioElementWithDescription gioElementWithDescription = new GioElementWithDescription() {
    };

    @Override
    public void addCharacters(String characters) {
        String xmlValue = mapOrDefault(gioElementWithDescription.getDescription(), XmlFragmentString::rawXml, "");
        XML.XmlSpace xmlSpace = mapOrDefault(gioElementWithDescription.getDescription(), XmlFragmentString::xmlSpace, XML.XmlSpace.default_);
        xmlValue += characters;
        XmlFragmentString xmlFrag = XmlFragmentString.of(xmlValue, xmlSpace);
        gioElementWithDescription.setDescription(xmlFrag);
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
