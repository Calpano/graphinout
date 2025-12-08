package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.json.JSON;

import java.util.Objects;

public final class JsonXmlString implements IJsonXmlString {

    private final IJsonFactory factory;
    private final String rawXml;
    private final JSON.XmlSpace xmlSpace;

    public JsonXmlString(IJsonFactory factory, String rawXml, JSON.XmlSpace xmlSpace) {
        this.factory = factory;
        this.rawXml = rawXml;
        this.xmlSpace = xmlSpace;
    }

    @Override
    public Object base() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        JsonXmlString that = (JsonXmlString) obj;
        return Objects.equals(this.factory, that.factory) && Objects.equals(this.rawXml, that.rawXml) && Objects.equals(this.xmlSpace, that.xmlSpace);
    }

    public IJsonFactory factory() {return factory;}

    @Override
    public int hashCode() {
        return Objects.hash(factory, rawXml, xmlSpace);
    }

    public String rawXml() {return rawXml;}

    @Override
    public String rawXmlString() {
        return rawXml;
    }

    @Override
    public String toString() {
        return "JsonXmlString[" + "factory=" + factory + ", " + "rawXml=" + rawXml + ", " + "xmlSpace=" + xmlSpace + ']';
    }

    public JSON.XmlSpace xmlSpace() {return xmlSpace;}

}
