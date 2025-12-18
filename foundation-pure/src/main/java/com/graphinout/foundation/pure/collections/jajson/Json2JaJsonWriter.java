package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.JsonException;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Consumer;

public class Json2JaJsonWriter implements JsonWriter {

    private final Stack<Object> stack = new Stack<>();
    private Object result;
    private Locator locator;
    private Consumer<ContentError> errorHandler;

    @Override
    public void arrayEnd() throws JsonException {
        if (stack.size() == 1) {
            result = stack.peek();
        }
        stack.pop();
    }

    @Override
    public void arrayStart() throws JsonException {
        stack.push(new ArrayList<>());
    }

    @Override
    public @Nullable Consumer<ContentError> contentErrorHandler() {
        return errorHandler;
    }

    @Override
    public void documentEnd() throws JsonException {
    }

    @Override
    public void documentStart() throws JsonException {
        stack.clear();
    }

    @Override
    public @Nullable Locator locator() {
        return locator;
    }

    @Override
    public void objectEnd() throws JsonException {
        if (stack.size() == 1) {
            result = stack.peek();
        }
        stack.pop();
    }

    @Override
    public void objectStart() throws JsonException {
        TreeMap<String, Object> map = new TreeMap<>();
        stack.push(map);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        top().accept(bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        top().accept(bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        top().accept(b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        top().accept(d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        top().accept(f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        top().accept(i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        stack.push(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        top().accept(l);
    }

    @Override
    public void onNull() throws JsonException {
        top().accept(null);
    }

    @Override
    public void onString(String s) throws JsonException {
        top().accept(s);
    }

    /**
     * Either a {@link List}, a {@link Map} or a primitive (String, Number, Boolean, null)
     */
    public @Nullable Object resultJsonRootObject() {
        return result;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void setLocator(Locator locator) {
        this.locator = locator;
    }

    private Consumer<Object> top() {
        if (stack.isEmpty()) {
            return stack::push;
        } else {
            Object o = stack.peek();
            if (o instanceof List) {
                //noinspection unchecked,rawtypes
                return v -> ((List) o).add(v);
            } else if (o instanceof String) {
                stack.pop();
                //noinspection unchecked,rawtypes
                return v -> ((Map) stack.peek()).put(o, v);
            }
        }
        throw new IllegalStateException();
    }

}
