package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

public class OcifAngle extends OcifType {

    final double value;

    public OcifAngle(double value) {
        if (value < -360 || value > 360) {
            throw new IllegalArgumentException("Angle must be between -360 and 360.");
        }
        this.value = value;
    }

    public static OcifAngle of(double d) {
        return new OcifAngle(d);
    }

    public static OcifAngle of(IJsonValue jsonValue) throws IllegalStateException {
        return new OcifAngle(jsonValue.asNumber().doubleValue());
    }

    @Override
    public IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createNumber(value);
    }

}
