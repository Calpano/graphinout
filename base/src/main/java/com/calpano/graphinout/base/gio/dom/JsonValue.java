package com.calpano.graphinout.base.gio.dom;

import javax.annotation.Nullable;

public interface JsonValue {

    static PrimitiveJsonValue.BigDecimalValue bigDecimalValue(java.math.BigDecimal value) {
        return new PrimitiveJsonValue.BigDecimalValue(value);
    }

    static PrimitiveJsonValue.BigIntegerValue bigIntegerValue(java.math.BigInteger value) {
        return new PrimitiveJsonValue.BigIntegerValue(value);
    }

    static PrimitiveJsonValue.BooleanValue booleanValue(boolean value) {
        return new PrimitiveJsonValue.BooleanValue(value);
    }

    static PrimitiveJsonValue.DoubleValue doubleValue(double value) {
        return new PrimitiveJsonValue.DoubleValue(value);
    }

    static PrimitiveJsonValue.FloatValue floatValue(float value) {
        return new PrimitiveJsonValue.FloatValue(value);
    }

    static PrimitiveJsonValue.IntegerValue intValue(int value) {
        return new PrimitiveJsonValue.IntegerValue(value);
    }

    static PrimitiveJsonValue.LongValue longValue(long value) {
        return new PrimitiveJsonValue.LongValue(value);
    }

    static PrimitiveJsonValue.NullValue nullValue() {
        return new PrimitiveJsonValue.NullValue();
    }

    static PrimitiveJsonValue.StringValue stringValue(String value) {
        return new PrimitiveJsonValue.StringValue(value);
    }

    @Nullable
    GioType type();

}
