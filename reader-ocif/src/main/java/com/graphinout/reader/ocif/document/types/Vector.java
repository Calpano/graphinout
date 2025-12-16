package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

public class Vector extends OcifType {

    final double[] values;

    public Vector(double... values) {
        if (values.length < 1 || values.length > 3) {
            throw new IllegalArgumentException("Vector must have 1, 2, or 3 values.");
        }
        this.values = values;
    }

    public static Vector of(IJsonValue jsonValue) throws IllegalStateException {
        if (jsonValue.isNumber()) {
            return new Vector(jsonValue.asNumber().doubleValue());
        } else if (jsonValue.isArray()) {
            double[] arrayValues = jsonValue.asArray().asDoubles();
            return new Vector(arrayValues);
        } else {
            throw new IllegalArgumentException("Invalid vector format. Expected a number or an array of numbers.");
        }
    }

    @Override
    IJsonValue toJson() {
        if (values.length == 1) {
            return IJsonFactory.INSTANCE.createNumber(values[0]);
        } else {
            return IJsonFactory.INSTANCE.createArrayMutable().add(values);
        }
    }

}
