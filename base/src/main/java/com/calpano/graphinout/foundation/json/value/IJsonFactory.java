package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JSON;
import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.xml.XML;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.slf4j.LoggerFactory.getLogger;

public interface IJsonFactory {

    Logger _log = getLogger(IJsonFactory.class);

    default IJsonArrayMutable asArrayMutable(IJsonArray array) {
        if (array instanceof IJsonArrayMutable arrayMutable) {
            return arrayMutable;
        }
        IJsonArrayMutable arrayMutable = createArrayMutable();
        array.forEach(arrayMutable::add);
        return arrayMutable;
    }

    default IJsonObjectMutable asObjectMutable(IJsonObject object) {
        if (object instanceof IJsonObjectMutable objectMutable) {
            return objectMutable;
        }
        IJsonObjectMutable objectMutable = createObjectMutable();
        object.forEach(objectMutable::addProperty);
        return objectMutable;
    }

    IJsonArray createArray();

    IJsonArrayAppendable createArrayAppendable();

    IJsonArrayMutable createArrayMutable();

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
     * @param string Is auto-trimmed for parsing as Boolean but kept intact for String creation.
     * @return JSON Boolean or -- as fall-back -- JSON String
     */
    default IJsonPrimitive createBooleanFromString(String string) {
        if (string.trim().equalsIgnoreCase("true")) {
            return createBoolean(true);
        } else if (string.trim().equalsIgnoreCase("false")) {
            return createBoolean(false);
        }
        // debatable
        if (string.isBlank()) return createBoolean(false);

        //unparseable boolean
        // _log.warn("Could not parse as boolean: '{}'", valueTrimmedMaybe);
        return createString(string);
    }

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

    /**
     * @param string to parse. Is auto-trimmed for parsing as Number but kept intact for String creation.
     * @return JSON Number or -- as fall-back -- JSON String
     */
    default IJsonPrimitive createNumberFromString(String string) {
        try {
            BigDecimal bd = new BigDecimal(string.trim());
            return createNumber(bd);
        } catch (Exception e) {
            _log.warn("Could not parse as number: '{}'", string);
        }
        return createString(string);
    }

    IJsonObject createObject();

    IJsonObjectAppendable createObjectAppendable();

    IJsonObjectMutable createObjectMutable();

    /**
     * @param desiredJsonType to create, if possible
     * @param value           might be null, empty, wrong type ...
     * @return the requested jsonType or string, if value cannot be parsed to given jsonType
     */
    default IJsonPrimitive createPrimitiveFromString(JsonType desiredJsonType, String value, boolean preserveSpace) {
        assert desiredJsonType.valueType() == JsonType.ValueType.Primitive;
        if (value == null) return createNull();
        String valueTrimmedMaybe = preserveSpace ? value : value.trim();
        return switch (desiredJsonType) {
            case Boolean -> createBooleanFromString(valueTrimmedMaybe);
            case Number -> createNumberFromString(valueTrimmedMaybe);
            case String -> createString(valueTrimmedMaybe);
            case XmlString ->
                    IJsonXmlString.of(this, value, preserveSpace ? JSON.XmlSpace.preserve : JSON.XmlSpace.auto);
            default -> throw new IllegalArgumentException("Unsupported type: " + desiredJsonType);
        };
    }

    /**
     * JSON String
     */
    IJsonPrimitive createString(String s);

    default IJsonXmlString createXmlString(String rawXml, XML.XmlSpace xmlSpace) {
        return IJsonXmlString.of(this, rawXml, xmlSpace.toJson_XmlSpace());
    }


}
