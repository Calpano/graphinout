package com.calpano.graphinout.base.gio.dom;

import javax.annotation.Nullable;

public abstract class PrimitiveJsonValue implements JsonValue {

    public static class BooleanValue extends PrimitiveJsonValue {

        protected BooleanValue(boolean value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonBoolean;
        }

    }

    public static class StringValue extends PrimitiveJsonValue {

        protected StringValue(String value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonString;
        }

    }

    public static class NullValue extends PrimitiveJsonValue {

        protected NullValue() {
            super(null);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNull;
        }

    }

    public static class IntegerValue extends PrimitiveJsonValue {

        protected IntegerValue(int value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }

    public static class LongValue extends PrimitiveJsonValue {

        protected LongValue(long value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }

    public static class FloatValue extends PrimitiveJsonValue {

        protected FloatValue(float value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }

    public static class DoubleValue extends PrimitiveJsonValue {

        protected DoubleValue(double value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }

    public static class BigIntegerValue extends PrimitiveJsonValue {

        protected BigIntegerValue(java.math.BigInteger value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }

    public static class BigDecimalValue extends PrimitiveJsonValue {

        protected BigDecimalValue(java.math.BigDecimal value) {
            super(value);
        }

        @Nullable
        @Override
        public GioType type() {
            return GioType.JsonNumber;
        }

    }


    private final Object value;

    protected PrimitiveJsonValue(Object value) {this.value = value;}

    Object value() {
        return value;
    }

}
