package com.calpano.graphinout.foundation.json.value;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface IJsonFactory {

    IJsonArray createArray();

    IJsonArrayAppendable createArrayAppendable();

    /**
     * Create JSON Number
     */
    IJsonPrimitive createBigDecimal(BigDecimal bigDecimal);

    /**
     * JSON Number
     */
    IJsonPrimitive createBigInteger(BigInteger bigInteger);

    /** Convenience method only used for writing JSON */
    default IJsonPrimitive createBoolean(@Nullable Boolean b) {
        return b == null ? createNull() : createBoolean(b.booleanValue());
    }

    /**
     * JSON Boolean
     */
    IJsonPrimitive createBoolean(boolean b);

    /**
     * JSON Number with decimals, which may be larger than Java {@link Float#MAX_VALUE}
     */
    IJsonPrimitive createDouble(double d);

    /**
     * JSON Number with decimals, which is less than or equal to Java {@link Float#MAX_VALUE}
     */
    IJsonPrimitive createFloat(float f);

    /** Convenience method only used for writing JSON */
    default IJsonPrimitive createFloat(@Nullable Float f) {
        return f == null ? createNull() : createFloat(f.floatValue());
    }

    /**
     * JSON Number without decimals, which is less than or equal to Java {@link Integer#MAX_VALUE}
     */
    IJsonPrimitive createInteger(int i);

    /** Convenience method only used for writing JSON */
    default IJsonPrimitive createInteger(@Nullable Integer i) {
        return i == null ? createNull() : createInteger(i.intValue());
    }

    /**
     * JSON Number without decimals, which may be larger than Java {@link Integer#MAX_VALUE}
     */
    IJsonPrimitive createLong(long l);

    /** Convenience method only used for writing JSON */
    default IJsonPrimitive createLong(@Nullable Long l) {
        return l == null ? createNull() : createLong(l.longValue());
    }

    /**
     * JSON Null
     */
    IJsonPrimitive createNull();

    default IJsonValue createNumber(Number value) {
        if (value instanceof BigDecimal) {
            return createBigDecimal((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return createBigInteger((BigInteger) value);
        } else if (value instanceof Double) {
            return createDouble(value.doubleValue());
        } else if (value instanceof Float) {
            return createFloat(value.floatValue());
        } else if (value instanceof Long) {
            return createLong(value.longValue());
        } else if (value instanceof Integer) {
            return createInteger(value.intValue());
        }
        throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
    }

    IJsonObject createObject();

    IJsonObjectAppendable createObjectAppendable();

    /**
     * JSON String
     */
    IJsonPrimitive createString(String s);

}
