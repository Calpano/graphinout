package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.slf4j.LoggerFactory.getLogger;

public interface IJsonFactory {

    Logger _log = getLogger(IJsonFactory.class);

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

    default IJsonPrimitive createNumber(Number value) {
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
     * @param jsonType to create, if possible
     * @param value    might be null, empty, wrong type ...
     * @return the requested jsonType or string, if value cannot be parsed to given jsonType
     */
    default IJsonPrimitive createPrimitiveFromString(JsonType jsonType, String value) {
        assert jsonType.valueType() == JsonType.ValueType.Primitive;
        if (value == null) return createNull();
        String valueTrimmed = value.trim();
        switch (jsonType) {
            case Boolean:
                if (valueTrimmed.equalsIgnoreCase("true")) {
                    return createBoolean(true);
                } else if (valueTrimmed.equalsIgnoreCase("false")) {
                    return createBoolean(false);
                }
                if (valueTrimmed.isBlank()) return createBoolean(false);

                //unparseable boolean
                _log.warn("Could not parse as boolean: '{}'", valueTrimmed);
                return createString(valueTrimmed);
            case Number:
                try {
                    BigDecimal bd = new BigDecimal(valueTrimmed);
                    return createNumber(bd);
                } catch (Exception e) {
                    _log.warn("Could not parse as number: '{}'", valueTrimmed);
                }
                return createString(valueTrimmed);
            case String:
                return createString(valueTrimmed);
            default:
                throw new IllegalArgumentException("Unsupported type: " + jsonType);
        }
    }

    /**
     * JSON String
     */
    IJsonPrimitive createString(String s);

}
