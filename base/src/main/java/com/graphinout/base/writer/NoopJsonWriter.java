package com.graphinout.base.writer;

import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NoopJsonWriter implements JsonWriter {

    @Override
    public void arrayEnd() {}

    @Override
    public void arrayStart() {}

    @Nullable
    @Override
    public Consumer<ContentError> contentErrorHandler() {
        return null;
    }

    @Override
    public void documentEnd() {}

    @Override
    public void documentStart() {}

    @Nullable
    @Override
    public Locator locator() {
        return null;
    }

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

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {

    }

    @Override
    public void setLocator(Locator locator) {

    }

}
