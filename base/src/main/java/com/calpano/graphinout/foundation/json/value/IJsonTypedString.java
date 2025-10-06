package com.calpano.graphinout.foundation.json.value;


import com.calpano.graphinout.foundation.json.JsonType;

import javax.annotation.Nullable;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrNull;

/**
 * Our custom JSON extension: Typed strings. This transports the fact that a string should not be XML encoded when
 * written. Instead, it contains #PCDATA, i.e., XML tags, which should be emitted as-is.
 * <p>
 * Encodes as <code>{ "type": "xml", "value": "foo<em>bar</em>" }</code>. Design alternative would have been to encode
 * as <code>{ "xml": "foo<em>bar</em>" }</code>.
 * <p>
 * A typed string MAY have other properties besides the defined ones.
 */
public interface IJsonTypedString {

    String TYPE = "type";
    String VALUE = "value";
    // common types
    String TYPE_XML = "xml";
    String TYPE_TEXT = "text";
    /** for JSON in JSON */
    String TYPE_JSON = "json";

    static IJsonTypedString asJsonTypedString(IJsonValue jsonValue) {
        assert isTypedString(jsonValue);
        return of(jsonValue.asObject());
    }

    @SuppressWarnings("RedundantIfStatement")
    static boolean isTypedString(IJsonValue jsonValue) {
        if (!jsonValue.isObject())
            return false;
        IJsonObject o = jsonValue.asObject();
        // check type
        IJsonValue type = o.get(TYPE);
        if (type == null || type.jsonType() != JsonType.String)
            return false;
        // check value
        if (!o.hasProperty(VALUE))
            return false;
        IJsonValue value = o.get(VALUE);
        if (value != null && value.jsonType() != JsonType.String)
            return false;

        return true;
    }

    static IJsonTypedString of(IJsonObject jsonObject) {
        assert isTypedString(jsonObject);
        return new IJsonTypedString() {
            public String type() {
                return jsonObject.get_(TYPE).asString();
            }

            public @Nullable String value() {
                return mapOrNull(jsonObject.get(VALUE), IJsonValue::asString);
            }
        };
    }

    static IJsonTypedString of(IJsonValue jsonValue) {
        switch (jsonValue.jsonType().valueType()) {
            case Object -> {
                if (isTypedString(jsonValue)) {
                    return of(jsonValue.asObject());
                } else {
                    throw new IllegalArgumentException("JsonObject cannot be interpreted as TypedString.");
                }
            }
            case Array -> throw new IllegalArgumentException("JsonArray cannot be interpreted as TypedString.");
            case Primitive -> {
                IJsonPrimitive primitive = jsonValue.asPrimitive();
                switch (primitive.jsonType()) {
                    case Boolean -> throw new IllegalArgumentException("Boolean cannot be interpreted as TypedString.");
                    case Number -> throw new IllegalArgumentException("Number cannot be interpreted as TypedString.");
                    case String -> {
                        String value = primitive.asString();
                        return of(TYPE_TEXT, value);
                    }
                }
            }
        }
        throw new AssertionError();
    }

    static IJsonTypedString of(String type, String value) {
        return new IJsonTypedString() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public String value() {
                return value;
            }
        };

    }

    String type();

    @Nullable
    String value();


}
