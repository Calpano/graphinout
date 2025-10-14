package com.calpano.graphinout.foundation.json.value;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonTypes {

    public static final BigDecimal BD_FLOAT_MAX = BigDecimal.valueOf(Float.MAX_VALUE);
    public static final BigDecimal BD_NEG_FLOAT_MAX = BigDecimal.valueOf(-Float.MAX_VALUE);
    public static final BigDecimal BD_DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal BD_NEG_DOUBLE_MAX = BigDecimal.valueOf(-Double.MAX_VALUE);

    public static final BigInteger BI_BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    public static final BigInteger BI_BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
    public static final BigInteger BI_SHORT_MIN = BigInteger.valueOf(Short.MIN_VALUE);
    public static final BigInteger BI_SHORT_MAX = BigInteger.valueOf(Short.MAX_VALUE);
    public static final BigInteger BI_INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    public static final BigInteger BI_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    public static final BigInteger BI_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    public static final BigInteger BI_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);


    /**
     * Parse a string to the smallest possible {@link Number} type.
     *
     * @param valueTrimmed the string to parse
     * @return a {@link Number}
     */
    public static Number parseNumber(String valueTrimmed) {
        BigDecimal bd = new BigDecimal(valueTrimmed);
        Class<?> type = strictestType(bd);
        if (type == Byte.class) return bd.byteValue();
        if (type == Short.class) return bd.shortValue();
        if (type == Integer.class) return bd.intValue();
        if (type == Long.class) return bd.longValue();
        if (type == BigInteger.class) return bd.toBigInteger();
        if (type == Float.class) return bd.floatValue();
        if (type == Double.class) return bd.doubleValue();
        return bd;
    }

    /**
     * What is the smallest/strictest type to fit the given number?
     *
     * @param n to fit
     * @return Class of best type
     */
    public static Class<?> strictestType(Number n) {
        if (n instanceof BigDecimal bd) {
            return strictestType(bd);
        } else if (n instanceof BigInteger bi) {
            return strictestIntegerType(bi);
        } else if (n instanceof Double d) {
            if (!Double.isFinite(d)) {
                return Double.class;
            }
            return strictestType(BigDecimal.valueOf(d));
        } else if (n instanceof Float f) {
            if (!Float.isFinite(f)) {
                return Float.class;
            }
            return strictestType(BigDecimal.valueOf(f.doubleValue()));
        } else if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
            return strictestIntegerType(BigInteger.valueOf(n.longValue()));
        }
        return Number.class; // Fallback
    }

    public static Class<?> strictestType(String value) {
        return strictestType(new BigDecimal(value));
    }

    private static Class<?> strictestType(BigDecimal bd) {
        BigDecimal stripped = bd.stripTrailingZeros();
        if (stripped.scale() <= 0) { // It's an integer
            return strictestIntegerType(stripped.toBigInteger());
        } else { // It's a decimal
            if (bd.compareTo(BD_NEG_FLOAT_MAX) >= 0 && bd.compareTo(BD_FLOAT_MAX) <= 0) {
                return Float.class;
            } else if (bd.compareTo(BD_NEG_DOUBLE_MAX) >= 0 && bd.compareTo(BD_DOUBLE_MAX) <= 0) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        }
    }

    private static Class<?> strictestIntegerType(BigInteger bi) {
        if (bi.compareTo(BI_BYTE_MIN) >= 0 && bi.compareTo(BI_BYTE_MAX) <= 0) {
            return Byte.class;
        } else if (bi.compareTo(BI_SHORT_MIN) >= 0 && bi.compareTo(BI_SHORT_MAX) <= 0) {
            return Short.class;
        } else if (bi.compareTo(BI_INT_MIN) >= 0 && bi.compareTo(BI_INT_MAX) <= 0) {
            return Integer.class;
        } else if (bi.compareTo(BI_LONG_MIN) >= 0 && bi.compareTo(BI_LONG_MAX) <= 0) {
            return Long.class;
        } else {
            return BigInteger.class;
        }
    }

}
