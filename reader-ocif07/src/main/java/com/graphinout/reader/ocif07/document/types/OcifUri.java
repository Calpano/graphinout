package com.graphinout.reader.ocif07.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.net.URISyntaxException;

public class OcifUri extends OcifType {

    final java.net.URI value;

    public OcifUri(String value) {
        try {
            this.value = new java.net.URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI format: " + value, e);
        }
    }

    public static OcifUri of(IJsonValue jsonValue) throws IllegalStateException {
        return new OcifUri(jsonValue.asString());
    }

    @Override
    public IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createString(value.toString());
    }

}
