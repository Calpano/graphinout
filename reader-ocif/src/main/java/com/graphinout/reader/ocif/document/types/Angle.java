package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

public class Angle extends OcifType {

    final double value;

    public Angle(double value) {
        if (value < -360 || value > 360) {
            throw new IllegalArgumentException("Angle must be between -360 and 360.");
        }
        this.value = value;
    }

    public static Angle of(IJsonValue jsonValue) throws IllegalStateException {
        return new Angle(jsonValue.asNumber().doubleValue());
    }

    @Override
    IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createNumber(value);
    }

}
