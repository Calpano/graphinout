package com.calpano.graphinout.foundation.json.value;


import com.calpano.graphinout.foundation.json.JsonType;

import javax.annotation.Nullable;
import java.util.Set;

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
public interface IJsonTypedString extends IJsonObject {

    String TYPE = "type";
    String VALUE = "value";
    // common types
    String TYPE_XML = "xml";

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
            @Override
            public Object base() {
                return jsonObject.base();
            }

            @Override
            public IJsonFactory factory() {
                return jsonObject.factory();
            }

            @Nullable
            @Override
            public IJsonValue get(String key) {
                return jsonObject.get(key);
            }

            @Override
            public Set<String> keys() {
                return jsonObject.keys();
            }
        };
    }

    default String type() {
        return this.get_(TYPE).asString();
    }

    default @Nullable String value() {
        return mapOrNull(this.get(VALUE), IJsonValue::asString);
    }


}
