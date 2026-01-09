package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.JsonException;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

// TODO move to another package
public class Json2JsonValueWriter implements JsonWriter {

    private final Stack<Object> stack = new Stack<>();
    private final IJsonFactory jsonFactory;
    private IJsonValue result;
    private Locator locator;
    private Consumer<ContentError> errorHandler;

    public Json2JsonValueWriter(IJsonFactory jsonFactory) {this.jsonFactory = jsonFactory;}

    @Override
    public void arrayEnd() throws JsonException {
        if (stack.size() == 1) {
            result = (IJsonValue) stack.peek();
        }
        stack.pop();
    }

    @Override
    public void arrayStart() throws JsonException {
        IJsonArrayMutable arrayMutable = jsonFactory.createArrayMutable();
        top().accept(arrayMutable);
        stack.push(arrayMutable);
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
            result = (IJsonValue) stack.peek();
        }
        stack.pop();
    }

    @Override
    public void objectStart() throws JsonException {
        IJsonObjectMutable objectMutable = jsonFactory.createObjectMutable();
        top().accept(objectMutable);
        stack.push(objectMutable);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        top().accept(jsonFactory.createBigDecimal(bigDecimal));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        top().accept(jsonFactory.createBigInteger(bigInteger));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        top().accept(jsonFactory.createBoolean(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        top().accept(jsonFactory.createDouble(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        top().accept(jsonFactory.createFloat(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        top().accept(jsonFactory.createInteger(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        stack.push(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        top().accept(jsonFactory.createLong(l));
    }

    @Override
    public void onNull() throws JsonException {
        top().accept(jsonFactory.createNull());
    }

    @Override
    public void onString(String s) throws JsonException {
        top().accept(jsonFactory.createString(s));
    }

    /**
     * Either a {@link List}, a {@link Map} or a primitive (String, Number, Boolean, null)
     */
    public @Nullable IJsonValue resultJsonRootObject() {
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

    private Consumer<IJsonValue> top() {
        if (stack.isEmpty()) {
            return v -> result = v;
        } else {
            Object o = stack.peek();
            if (o instanceof IJsonArrayMutable) {
                return v -> ((IJsonArrayMutable) o).add(v);
            } else if (o instanceof String) {
                String key = (String) o;
                stack.pop();
                Object peek = stack.peek();
                assert peek instanceof IJsonObjectMutable;
                IJsonObjectMutable objectMutable = (IJsonObjectMutable) peek;
                return v -> objectMutable.add(key, v);
            }
            throw new IllegalStateException("Expected top={none|array|string} but was "+o.getClass().getSimpleName());
        }
    }

}
