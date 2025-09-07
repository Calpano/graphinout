package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.JsonException;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonPrimitiveWriter extends HasJsonValueWriter {

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
     * JSON Null
     */
    void onNull() throws JsonException;

    /**
     * JSON String
     */
    void onString(String s) throws JsonException;


}
