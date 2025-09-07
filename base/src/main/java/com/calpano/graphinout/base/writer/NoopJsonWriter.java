package com.calpano.graphinout.base.writer;

import com.calpano.graphinout.foundation.json.stream.JsonWriter;

public class NoopJsonWriter implements JsonWriter {

    @Override
    public void arrayEnd() {}

    @Override
    public void arrayStart() {}

    @Override
    public void documentEnd() {}

    @Override
    public void documentStart() {}

    @Override
    public void objectEnd() {}

    @Override
    public void objectStart() {}

    @Override
    public void onBigDecimal(java.math.BigDecimal bigDecimal) {}

    @Override
    public void onBigInteger(java.math.BigInteger bigIntegerValue) {}

    @Override
    public void onBoolean(boolean b) {}

    @Override
    public void onDouble(double d) {}

    @Override
    public void onFloat(float f) {}

    @Override
    public void onInteger(int i) {}

    @Override
    public void onKey(String key) {}

    @Override
    public void onLong(long l) {}

    @Override
    public void onNull() {}

    @Override
    public void onString(String s) {}

}
