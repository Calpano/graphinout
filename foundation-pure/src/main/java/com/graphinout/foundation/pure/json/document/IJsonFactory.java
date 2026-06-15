package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.json.JSON;
import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.pure.log.Logger;
import com.graphinout.foundation.pure.log.LoggerFactory;
import com.graphinout.foundation.pure.xml.XML;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Collector;


/**
 * Factory for creating JSON values (objects, arrays, primitives), in mutable and immutable form.
 */
public interface IJsonFactory {

    Logger _log = LoggerFactory.getLogger(IJsonFactory.class);
    IJsonFactory INSTANCE = new JavaJsonFactory();

    default Collector<? super IJsonValue, IJsonArrayMutable, IJsonArray> arrayCollector() {
        return Collector.of(this::createArrayMutable, //
                IJsonArrayAppendable::add, //
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, a -> a);
    }

    default IJsonArrayMutable asArrayMutable(IJsonArray array) {
        if (array instanceof IJsonArrayMutable) {
            return (IJsonArrayMutable) array;
        }
        IJsonArrayMutable arrayMutable = createArrayMutable();
        array.forEach(arrayMutable::add);
        return arrayMutable;
    }

    /**
     * @return if it can be casted into a mutable version, return this instanced but type-casted. Otherwise, create a
     * mutable version and copy the properties.
     */
    default IJsonObjectMutable asObjectMutable(IJsonObject object) {
        if (object instanceof IJsonObjectMutable) {
            return (IJsonObjectMutable) object;
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
        if (Java9.String.isBlank(string)) return createBoolean(false);

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
        switch (desiredJsonType) {
            case Boolean:
                return createBooleanFromString(valueTrimmedMaybe);
            case Number:
                return createNumberFromString(valueTrimmedMaybe);
            case String:
                return createString(valueTrimmedMaybe);
            case XmlString:
                return IJsonXmlString.of(this, value, preserveSpace ? JSON.XmlSpace.preserve : JSON.XmlSpace.auto);
            default:
                throw new IllegalArgumentException("Unsupported type: " + desiredJsonType);
        }
    }

    /**
     * JSON String
     */
    IJsonPrimitive createString(String s);

    default IJsonXmlString createXmlString(String rawXml, XML.XmlSpace xmlSpace) {
        return IJsonXmlString.of(this, rawXml, xmlSpace.toJson_XmlSpace());
    }

}
