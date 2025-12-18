package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A vector for 2 or 3 dimensions
 */
public class OcifVector23D extends OcifType {

    public static final OcifVector23D ZERO_2D = new OcifVector23D(new double[2]);
    final double[] values;

    public OcifVector23D(double[] values) {this.values = values;}

    public static OcifVector23D of(@NonNull IJsonValue jsonValue) throws IllegalStateException {
        if (jsonValue.isNumber()) {
            // TODO remove from spec?
            // syntax shortcut: copy this number into all (2? 3?) dimensions
            double value = jsonValue.asNumber().doubleValue();
            return new OcifVector23D(new double[]{value, value, value});
        }

        double[] values = jsonValue.asArray().asDoubles();
        if (values.length == 2 || values.length == 3) {
            return new OcifVector23D(values);
        } else {
            throw new IllegalArgumentException("Invalid position. Expect 2 or 3 values but got " + values.length);
        }
    }

    public static OcifVector23D of(@Nullable IJsonValue jsonValue, OcifVector23D defaultValue) {
        if (jsonValue == null) return defaultValue;
        return of(jsonValue);
    }

    @Override
    public IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createArrayMutable().addAll(values);
    }

}
