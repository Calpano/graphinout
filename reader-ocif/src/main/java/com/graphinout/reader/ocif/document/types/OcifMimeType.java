package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.regex.Pattern;

public class OcifMimeType extends OcifType {

    // A simple regex for MIME type validation, can be made more robust if needed
    private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("^[a-zA-Z]+/[a-zA-Z0-9.+-]+$");

    final String value;

    public OcifMimeType(String value) {
        if (value == null || !MIME_TYPE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid MIME type format.");
        }
        this.value = value;
    }

    public static OcifMimeType of(IJsonValue jsonValue) throws IllegalStateException {
        return new OcifMimeType(jsonValue.asString());
    }

    @Override
    IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createString(value);
    }

}
