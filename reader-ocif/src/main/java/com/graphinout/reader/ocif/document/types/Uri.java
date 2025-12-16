package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.net.URISyntaxException;

public class Uri extends OcifType {

    final java.net.URI value;

    public Uri(String value) {
        try {
            this.value = new java.net.URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI format: " + value, e);
        }
    }

    public static Uri of(IJsonValue jsonValue) throws IllegalStateException {
        return new Uri(jsonValue.asString());
    }

    @Override
    IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createString(value.toString());
    }

}
