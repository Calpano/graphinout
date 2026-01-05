package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

public class OcifId extends OcifType {

    final String value;

    public OcifId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        if (value.startsWith("#")) {
            throw new IllegalArgumentException("ID cannot start with '#'.");
        }
        // Basic validation for control characters, more comprehensive validation can be added if needed
        if (value.matches(".*[\\p{Cntrl}].*")) {
            throw new IllegalArgumentException("ID cannot contain control characters.");
        }
        this.value = value;
    }

    public static OcifId of(IJsonValue jsonValue) throws IllegalStateException {
        return new OcifId(jsonValue.asString());
    }

    @Override
    public IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createString(value);
    }

}
