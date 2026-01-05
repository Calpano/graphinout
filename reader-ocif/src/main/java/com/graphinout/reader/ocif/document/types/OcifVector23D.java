package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * A vector for 2 or 3 dimensions
 */
public class OcifVector23D extends OcifType {

    public static final OcifVector23D ZERO_2D = new OcifVector23D(new double[2]);
    private double x;
    private double y;
    private Double z; // optional

    public OcifVector23D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public OcifVector23D(double x, double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OcifVector23D(double[] values) {
        assert values != null;
        assert values.length == 2 || values.length == 3;
        this.x = values[0];
        this.y = values[1];
        if (values.length == 3) {
            this.z = values[2];
        }
    }

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
    public IJsonArray toJson() {
        return factory().createArrayMutable().addAll(z == null ? //
                new double[]{x, y} : //
                new double[]{x, y, z});
    }

    public double x() {return x;}

    public void x(double x) {this.x = x;}

    public double y() {return y;}

    public void y(double y) {this.y = y;}

    public Double z() {return z;}

    public void z(Double z) {this.z = z;}


}
