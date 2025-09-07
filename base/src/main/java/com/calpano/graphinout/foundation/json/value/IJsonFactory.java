package com.calpano.graphinout.foundation.json.value;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface IJsonFactory {

    IJsonArray createArray();

    IAppendableJsonArray createArrayAppendable();

    /**
     * Create JSON Number
     */
    IJsonValue createBigDecimal(BigDecimal bigDecimal);

    /**
     * JSON Number
     */
    IJsonValue createBigInteger(BigInteger bigInteger);

    /** Convenience method only used for writing JSON */
    default IJsonValue createBoolean(@Nullable Boolean b) {
        return b == null ? createNull() : createBoolean(b.booleanValue());
    }

    /**
     * JSON Boolean
     */
    IJsonValue createBoolean(boolean b);

    /**
     * JSON Number with decimals, which may be larger than Java {@link Float#MAX_VALUE}
     */
    IJsonValue createDouble(double d);

    /**
     * JSON Number with decimals, which is less than or equal to Java {@link Float#MAX_VALUE}
     */
    IJsonValue createFloat(float f);

    /** Convenience method only used for writing JSON */
    default IJsonValue createFloat(@Nullable Float f) {
        return f == null ? createNull() : createFloat(f.floatValue());
    }

    /**
     * JSON Number without decimals, which is less than or equal to Java {@link Integer#MAX_VALUE}
     */
    IJsonValue createInteger(int i);

    /** Convenience method only used for writing JSON */
    default IJsonValue createInteger(@Nullable Integer i) {
        return i == null ? createNull() : createInteger(i.intValue());
    }

    /**
     * JSON Number without decimals, which may be larger than Java {@link Integer#MAX_VALUE}
     */
    IJsonValue createLong(long l);

    /** Convenience method only used for writing JSON */
    default IJsonValue createLong(@Nullable Long l) {
        return l == null ? createNull() : createLong(l.longValue());
    }

    /**
     * JSON Null
     */
    IJsonValue createNull();

    IJsonObject createObject();

    IAppendableJsonObject createObjectAppendable();

    /**
     * JSON String
     */
    IJsonValue createString(String s);

}
