package com.graphinout.foundation.pure.json.writer;


import com.graphinout.foundation.pure.json.JsonException;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Streaming writer for scalar JSON values (string, number, boolean, null).
 */
public interface JsonPrimitiveWriter extends IHasJsonValueWriter {

    /**
     * JSON Number
     */
    void onBigDecimal(BigDecimal bigDecimal) throws JsonException;

    /**
     * JSON Number
     */
    void onBigInteger(BigInteger bigInteger) throws JsonException;

    /**
     * JSON Boolean
     */
    void onBoolean(boolean b) throws JsonException;

    /** Convenience method only used for writing JSON */
    default void onBoolean(@Nullable Boolean b) throws JsonException {
        if (b == null) onNull();
        else onBoolean(b.booleanValue());
    }

    /**
     * JSON Number with decimals, which may be larger than Java {@link Float#MAX_VALUE}
     */
    void onDouble(double d) throws JsonException;

    /**
     * JSON Number with decimals, which is less than or equal to Java {@link Float#MAX_VALUE}
     */
    void onFloat(float f) throws JsonException;

    /** Convenience method only used for writing JSON */
    default void onFloat(@Nullable Float f) throws JsonException {
        if (f == null) onNull();
        else onFloat(f.floatValue());
    }

    /**
     * JSON Number without decimals, which is less than or equal to Java {@link Integer#MAX_VALUE}
     */
    void onInteger(int i) throws JsonException;

    /** Convenience method only used for writing JSON */
    default void onInteger(@Nullable Integer i) throws JsonException {
        if (i == null) onNull();
        else onInteger(i.intValue());
    }

    /**
     * JSON Number without decimals, which may be larger than Java {@link Integer#MAX_VALUE}
     */
    void onLong(long l) throws JsonException;

    /** Convenience method only used for writing JSON */
    default void onLong(@Nullable Long l) throws JsonException {
        if (l == null) onNull();
        else onLong(l.longValue());
    }

    /**
     * JSON Number of any Java number type. This is the one place that maps Java number types to the typed callbacks:
     * {@link Long} → {@link #onLong(long)}, {@link Integer}, {@link Short}, {@link Byte} → {@link #onInteger(int)},
     * {@link Double} → {@link #onDouble(double)}, {@link Float} → {@link #onFloat(float)}, {@link BigInteger} →
     * {@link #onBigInteger(BigInteger)}, {@link BigDecimal} → {@link #onBigDecimal(BigDecimal)}.
     *
     * @throws IllegalArgumentException for other {@link Number} types, e.g. {@code AtomicLong}
     */
    default void onNumber(Number n) throws JsonException {
        if (n instanceof Long) {
            onLong(n.longValue());
        } else if (n instanceof Integer || n instanceof Short || n instanceof Byte) {
            onInteger(n.intValue());
        } else if (n instanceof Double) {
            onDouble(n.doubleValue());
        } else if (n instanceof Float) {
            onFloat(n.floatValue());
        } else if (n instanceof BigInteger) {
            onBigInteger((BigInteger) n);
        } else if (n instanceof BigDecimal) {
            onBigDecimal((BigDecimal) n);
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + n.getClass().getName());
        }
    }

    /**
     * JSON Null
     */
    void onNull() throws JsonException;

    /**
     * JSON String
     */
    void onString(String s) throws JsonException;

}
