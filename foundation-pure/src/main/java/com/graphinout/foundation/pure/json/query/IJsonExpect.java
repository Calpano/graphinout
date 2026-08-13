package com.graphinout.foundation.pure.json.query;

import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public interface IJsonExpect {

    static IJsonExpect expectAbsent() {return Objects::isNull;}

    static IJsonExpect expectPresent() {return Objects::nonNull;}

    static IJsonExpect expectStringValue(String expected) {
        return expectValue(JavaJsonFactory.INSTANCE.createString(expected));
    }

    static IJsonExpect expectType(JsonType expectedType) {return v -> v != null && v.jsonType() == expectedType;}

    static IJsonExpect expectValue(IJsonValue expectedValue) {return v -> v != null && v.equals(expectedValue);}

    default IJsonExpect and(IJsonExpect other) {return v -> this.matches(v) && other.matches(v);}

    /**
     * @param value a {@code null} is the absence of the path from the queried JSON value. If the value has
     *              {@link JsonType} null, there is a JSON entry containing {@code null}.
     * @return true if expectation is met.
     */
    boolean matches(@Nullable IJsonValue value);

    default IJsonExpect negate() {return v -> !this.matches(v);}

    default IJsonExpect or(IJsonExpect other) {return v -> this.matches(v) || other.matches(v);}

}
