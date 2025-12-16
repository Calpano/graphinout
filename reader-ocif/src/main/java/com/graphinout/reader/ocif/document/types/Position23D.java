package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

public class Position23D extends OcifType {

    final double[] values;

    public Position23D(double[] values) {this.values = values;}

    public static Position23D of(IJsonValue jsonValue) throws IllegalStateException {
        double[] values = jsonValue.asArray().asDoubles();
        if (values.length == 2 || values.length == 3) {
            return new Position23D(values);
        } else {
            throw new IllegalArgumentException("Invalid position. Expect 2 or 3 values but got " + values.length);
        }
    }

    @Override
    IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createArrayMutable().add(values);
    }

}
