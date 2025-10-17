package com.graphinout.foundation.json.value;

import com.graphinout.foundation.json.JSON;

public record JsonXmlString(IJsonFactory factory, String rawXml, JSON.XmlSpace xmlSpace) implements IJsonXmlString {

    @Override
    public Object base() {
        return this;
    }

    @Override
    public String rawXmlString() {
        return rawXml;
    }

}
